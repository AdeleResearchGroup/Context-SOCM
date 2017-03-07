package fr.liglab.adele.cream.runtime.handler.relation;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.model.Relation;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.ipojo.dependency.interceptors.DependencyInterceptor;
import org.apache.felix.ipojo.dependency.interceptors.ServiceTrackingInterceptor;
import org.apache.felix.ipojo.dependency.interceptors.TransformedServiceReference;
import org.apache.felix.ipojo.handlers.dependency.Dependency;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.util.DependencyModel;
import org.osgi.framework.BundleContext;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Handler(name = HandlerReference.RELATION_HANDLER, namespace = HandlerReference.NAMESPACE)
@Provides(specifications = ServiceTrackingInterceptor.class)

public class RelationHandler extends PrimitiveHandler implements ServiceTrackingInterceptor {

    /**
     * The handled fields of the instance
     */
    private final Map<String, String> fieldToRelation = new HashMap<>();
    /**
     * The handled dependencies of the instance
     */
    private final Map<DependencyModel, String> dependencyToRelation = new ConcurrentHashMap<>();
    /**
     * For each relation defined in the context entity, the target spec linked to this instance
     */
    private final Map<String, Set<String>> relationTargets = new HashMap<>();
    /**
     * Filter used to match the dependencies we are interested in
     */
    @ServiceProperty(name = DependencyInterceptor.TARGET_PROPERTY)
    private String dependencyFilter;
    /**
     * The context id of the associated instance
     */
    private String entityId;

    /**
     * Filter service dependencies to include only related context entities
     */
    @Override
    public <S> TransformedServiceReference<S> accept(DependencyModel dependency, BundleContext context, TransformedServiceReference<S> reference) {

        String relation = dependencyToRelation.get(dependency);

		/*
         * skip dependencies not associated to a relation
		 */
        if (relation == null)
            return reference;
		
		/*
		 * Filter candidates that are not context entities or are not related to this instance
		 */
        String targetEntity = (String) reference.get(ContextEntity.CONTEXT_ENTITY_ID);
        if (targetEntity == null || !relationTargets.get(relation).contains(targetEntity)) {
            return null;
        }
		
		/*
		 * Otherwise, accept candidate
		 */
        return reference;
    }

    @Override
    public void start() {
        //Do nothing
    }

    @Override
    public void stop() {
        //Do nothing
    }


    @Override
    public void configure(Element metadata, @SuppressWarnings("rawtypes") Dictionary configuration) throws ConfigurationException {

        entityId = (String) configuration.get(ContextEntity.CONTEXT_ENTITY_ID);

        InstanceManager instanceManager = getInstanceManager();
        String componentName = instanceManager.getClassName();

        String instanceName = instanceManager.getInstanceName();
        dependencyFilter = "(" + Factory.INSTANCE_NAME_PROPERTY + "=" + instanceName + ")";

        Element[] relationFields = metadata.getElements(HandlerReference.RELATION_HANDLER, HandlerReference.NAMESPACE);

        /*
         * Configure the list of handled fields 
         */
        for (Element relationField : relationFields) {


            String fieldName = relationField.getAttribute("field");
            FieldMetadata field = getPojoMetadata().getField(fieldName);

            if (field == null) {
                throw new ConfigurationException("Malformed Manifest : the specified relation field '" + fieldName + "' is not defined in class " + componentName);
            }

            String relation = relationField.getAttribute("relation");

            if (relation == null) {
                throw new ConfigurationException("Malformed Manifest : the relation is not specified for field '" + fieldName + "' in class " + componentName);
            }
        	
        	/*
        	 * register the mapping of field to relation
        	 */
            fieldToRelation.put(fieldName, relation);
        	
        	/*
        	 * initialize the target list for the relation
        	 */
            if (!relationTargets.containsKey(relation)) {
                relationTargets.put(relation, ConcurrentHashMap.newKeySet());
            }
        }
    }

    /**
     * Keep track of new relations of this instance
     */
    @Bind(id = "relation", aggregate = true, optional = true, policy = BindingPolicy.DYNAMIC, proxy = false)
    public void addRelation(Relation relation) {

        if (!relation.getSource().equals(entityId)) {
            return;
        }

        Set<String> targets = relationTargets.get(relation.getName());
        if (targets != null) {
            targets.add(relation.getTarget());
            recalculateDependencies(relation);
        }
    }

    /**
     * Keep track of removed relations of this instance
     */
    @Unbind(id = "relation")
    public void removeRelation(Relation relation) {

        if (!relation.getSource().equals(entityId)) {
            return;
        }

        Set<String> targets = relationTargets.get(relation.getName());
        if (targets != null) {
            targets.remove(relation.getTarget());
            recalculateDependencies(relation);
        }
    }

    private void recalculateDependencies(Relation relation) {
        if (getInstanceManager().getState() > InstanceManager.INVALID) {
            for (Map.Entry<DependencyModel, String> dependencyEntry : dependencyToRelation.entrySet()) {
                if (dependencyEntry.getValue().equals(relation.getName())) {
                    dependencyEntry.getKey().invalidateMatchingServices();
                }
            }
        }
    }

    /**
     * Add a dependency to the list of handled dependencies
     */
    @Override
    public void open(DependencyModel dependency) {
		
		/*
		 * If the dependency is associated to one of the handled relation fields, we intercept
		 * this dependency
		 */
        if (dependency instanceof Dependency) {
            String relation = fieldToRelation.get(((Dependency) dependency).getField());
            if (relation != null) {
                dependencyToRelation.put(dependency, relation);
            }
        }

    }

    /**
     * Stop handling dependency
     */
    @Override
    public void close(DependencyModel dependency) {
        dependencyToRelation.remove(dependency);
    }


}