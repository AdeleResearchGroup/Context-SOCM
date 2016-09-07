package fr.liglab.adele.cream.requirement.handler.impl;

import fr.liglab.adele.cream.facilities.ipojo.annotation.FacilitiesHandlerReference;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.dependency.interceptors.DependencyInterceptor;
import org.apache.felix.ipojo.dependency.interceptors.ServiceTrackingInterceptor;
import org.apache.felix.ipojo.dependency.interceptors.TransformedServiceReference;
import org.apache.felix.ipojo.handlers.dependency.Dependency;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.ParseUtils;
import org.apache.felix.ipojo.util.DependencyModel;
import org.osgi.framework.BundleContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Handler(name = FacilitiesHandlerReference.CONTEXT_REQUIREMENT_HANDLER_NAME, namespace = FacilitiesHandlerReference.FACILITIES_HANDLER_NAMESPACE)
@Provides(specifications = ServiceTrackingInterceptor.class)
public class ContextRequierementHandler extends PrimitiveHandler implements ServiceTrackingInterceptor {


    /**
     * Filter used to match the dependencies we are interested in
     */
    @ServiceProperty(name= DependencyInterceptor.TARGET_PROPERTY)
    private String dependencyFilter;

    private final Map<String,List<String>> fieldToSpecs = new HashMap<>();

    private final Map<DependencyModel,List<String>> dependencyToSpecs = new ConcurrentHashMap<>();

    @Override
    public <S> TransformedServiceReference<S> accept(DependencyModel dependency, BundleContext context, TransformedServiceReference<S> ref) {
        List<String> specsToCheck = dependencyToSpecs.get(dependency);

		/*
		 * skip dependencies not associated to a CS to check
		 */
        if (specsToCheck == null){
            return ref;
        }

        String[] specs = (String[])ref.get("objectClass");
        List<String> listOfRefSpec = Arrays.asList(specs);
        for (String specTocheck : specsToCheck){
            if (!listOfRefSpec.contains(specTocheck)){
                return null;
            }
        }

        return ref;
    }

    @Override
    public void open(DependencyModel dependency) {

        if (dependency instanceof Dependency) {
            List<String> specs = fieldToSpecs.get(((Dependency) dependency).getField());
            if (specs != null) {
                dependencyToSpecs.put(dependency,specs);
            }
        }

    }

    @Override
    public void close(DependencyModel dependency) {
        dependencyToSpecs.remove(dependency);
    }

    @Override
    public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
        InstanceManager instanceManager = getInstanceManager();
        String componentName			= instanceManager.getClassName();

        String instanceName				= instanceManager.getInstanceName();
        dependencyFilter				= "("+ Factory.INSTANCE_NAME_PROPERTY+"="+instanceName+")";

        Element[] handlerElements = metadata.getElements(FacilitiesHandlerReference.CONTEXT_REQUIREMENT_HANDLER_NAME, FacilitiesHandlerReference.FACILITIES_HANDLER_NAMESPACE);

        for (Element handlerElement : handlerElements){

            String handlerSpec= handlerElement.getAttribute(FacilitiesHandlerReference.CONTEXT_REQUIREMENT_SPEC_ATTRIBUTE_NAME);

            String fieldName	= handlerElement.getAttribute("field");
            FieldMetadata field	= getPojoMetadata().getField(fieldName);

            if (field == null) {
                throw new ConfigurationException("Malformed Manifest : the specified Context Requirement field '"+fieldName+"' is not defined in class "+componentName);
            }


            List<String> listOfSpec = ParseUtils.parseArraysAsList(handlerSpec);

            if (listOfSpec == null || listOfSpec.isEmpty()){
                throw new ConfigurationException("Malformed Manifest : the specified Context Requirement spec is null or empty in class "+componentName);
            }

            fieldToSpecs.put(fieldName,listOfSpec);
        }

    }

    @Override
    public  void stop() {

    }

    @Override
    public  void start() {

    }
}
