package fr.liglab.adele.cream.runtime.handler.creator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.osgi.framework.ServiceReference;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.annotations.provider.Creator;

import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.model.Relation;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;

import fr.liglab.adele.cream.runtime.handler.entity.EntityStateHandler;
import fr.liglab.adele.cream.runtime.model.impl.RelationImpl;



@Handler(name = HandlerReference.CREATOR_HANDLER, namespace = HandlerReference.NAMESPACE)
@Provides(specifications = {EntityProvider.class, RelationProvider.class})

public class CreatorHandler extends PrimitiveHandler implements EntityProvider, RelationProvider {

    private final Set<String> dynamicFields 				= new HashSet<>();
    private final Map<String, String> fieldToContext 		= new HashMap<>();

    private final Map<String, ComponentCreator> creators 	= new HashMap<>();

    @ServiceProperty(name="provided", value="")
    private String[] provided;

  	private final Function<Factory,Creator.Entity<?>> dynamicCreator = (factory) -> {

  		EntityCreator creator = (EntityCreator) creators.get(factory.getName());
  		if (creator != null) {
  			return creator;
  		}
  		
  		creator = instantiateEntityCreator(factory.getName(),factory);
		return creator;
	};
    
    @Override
    public void configure(Element metadata, @SuppressWarnings("rawtypes") Dictionary configuration) throws ConfigurationException {

        InstanceManager instanceManager = getInstanceManager();

        Element[] creatorElements = metadata.getElements(HandlerReference.CREATOR_HANDLER, HandlerReference.NAMESPACE);

        for (Element creator : creatorElements) {


            String fieldName = creator.getAttribute("field");
            String componentName = instanceManager.getClassName();

            FieldMetadata field = getPojoMetadata().getField(fieldName);

            if (field == null) {
                throw new ConfigurationException("Malformed Manifest : the specified creator field '" + fieldName + "' is not defined in class " + componentName);
            }

            boolean isStatic	= creator.getAttribute("dynamic") == null || ! Boolean.valueOf(creator.getAttribute("dynamic"));
            String entity 		= creator.getAttribute("entity");
            String relation 	= creator.getAttribute("relation");
            
            if (isStatic && entity == null && relation == null) {
                throw new ConfigurationException("Malformed Manifest : the creator entity or relation is not specified for field '" + fieldName + "' in class " + componentName);
            }

            if (isStatic && entity == null && relation != null) {
                throw new ConfigurationException("Malformed Manifest : the source for relation creator is not specified for field '" + fieldName + "' in class " + componentName);
            }
            
            if (isStatic && entity != null && relation == null) {
                instantiateEntityCreator(entity,null);
                fieldToContext.put(fieldName, entity);
            }

            if (isStatic && entity != null && relation != null) {
                instantiateRelationCreator(relation);
                fieldToContext.put(fieldName, relation);
            }

            if (!isStatic) {
                dynamicFields.add(fieldName);
            }

            instanceManager.register(getPojoMetadata().getField(fieldName), this);
        }
    }

    /**
     * Instantiate, if necessary, the creator associated with a given entity
     */
    private EntityCreator instantiateEntityCreator(String entity, Factory factory) {

        ComponentCreator creator = creators.get(entity);
        if (creator == null) {
            creator = new EntityCreator(entity);
            creators.put(entity, creator);

            if (factory != null) {
                creator.bindFactory(factory);
            }

            provided = creators.keySet().toArray(new String[0]);
        }
        
        return (EntityCreator) creator;
    }

    /**
     * Instantiate, if necessary, the creator associated with a given relation
     */
    private RelationCreator instantiateRelationCreator(String relation) {

        ComponentCreator creator = creators.get(relation);
        if (creator == null) {
            creator = new RelationCreator(relation);
            creators.put(relation, creator);

            provided = creators.keySet().toArray(new String[0]);
        }
        
        return (RelationCreator) creator;

    }

    @Override
    public Object onGet(Object pojo, String fieldName, Object value) {
        return dynamicFields.contains(fieldName) ? dynamicCreator : creators.get(fieldToContext.get(fieldName));
    }

    @Override
    public void onSet(Object pojo, String fieldName, Object value) {
        //Do nothnig
    }

    /**
     * Binds an iPOJO factory, it verifies that the factory is not required by any
     */
    @Bind(id = "ipojo.factory", aggregate = true, proxy = false, optional = true)
    public void bindFactory(Factory factory, ServiceReference<Factory> reference) {
        for (ComponentCreator creator : creators.values()) {
            if (creator.shouldBind(reference)) {
                creator.bindFactory(factory);
            }
        }
    }

    @Unbind(id = "ipojo.factory")
    public void unbindFactory(Factory factory, ServiceReference<Factory> reference) {
        for (ComponentCreator creator : creators.values()) {
            if (creator.shouldBind(reference)) {
                creator.unbindFactory();
            }
        }
    }

    @Override
    public void start() {
        //do nothing
    }

    @Override
    public void stop() {
        //do nothing
    }


    @Override
    public String getName() {
        return getInstanceManager().getClassName();
    }

    @Override
    public Set<String> getProvidedEntities() {
        return creators.keySet().stream()
                .filter(item -> creators.get(item) instanceof EntityCreator)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getProvidedRelations() {
        return creators.keySet().stream()
                .filter(item -> creators.get(item) instanceof RelationCreator)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isEnabled(String contextItem) {
        return creators.get(contextItem) != null && creators.get(contextItem).isEnabled();
    }

    @Override
    public boolean enable(String contextItem) {
        return creators.get(contextItem) != null && creators.get(contextItem).setEnabled(true);
    }

    @Override
    public boolean disable(String contextItem) {
        return creators.get(contextItem) != null && creators.get(contextItem).setEnabled(false);
    }


	@Override
	public Set<String> getProvidedServices(String entity) {
		
		Optional<ComponentCreator> creator = Optional.ofNullable(creators.get(entity));
		
		String[] services = creator.map(ComponentCreator::getComponentDescrition)
								.map(ComponentTypeDescription::getprovidedServiceSpecification)
								.orElse(new String[0]);
		
		return new HashSet<>(Arrays.asList(services));
	}

	@Override
	public Set<String> getInstances(String entity) {
		return Optional.ofNullable(creators.get(entity)).map(ComponentCreator::ids).orElse(Collections.emptySet());
	}

    @Override
    public HandlerDescription getDescription() {
        return new EntityCreatorHandlerDescription();
    }

    private static final String entityId(Object pojo) throws IllegalArgumentException {

    	if (pojo != null && pojo instanceof Pojo) {
    		
    		ContextEntity entity = EntityStateHandler.getContextEntity((Pojo) pojo);
    		
    		if (entity != null) {
               	return entity.getId();
    		}
    	}
    	
        throw new IllegalArgumentException("object "+ pojo + "is not a context entity");

    }


    private static class RelationCreator extends ComponentCreator implements Creator.Relation<Object,Object> {

        /**
         * The relation created by this factory
         */
        private final String relation;

        protected RelationCreator(String relation) {
            this.relation = relation;
        }

        @Override
        public String getDescription() {
        	return "Relation "+relation;
        }

        @Override
        public boolean shouldBind(ServiceReference<Factory> referenceFactory) {
            String factory = (String) referenceFactory.getProperty("factory.name");
            return factory != null && factory.equals(RelationImpl.class.getName());
        }

        private final String id(String sourceId, String targetId) {
            return sourceId + "--"+ relation +"--" + targetId;
        }

        @Override
        public void link(String sourceId, String targetId) {

            String linkId 		= id(sourceId,targetId);
            
            if (get(linkId) != null) {
                throw new IllegalArgumentException("Relation " + relation + " from " + sourceId + " to " + targetId + " already created");
            }

            Dictionary<String, Object> configuration = new Hashtable<>();

            configuration.put("instance.name", linkId);
            configuration.put("relation.id", relation);
            configuration.put("relation.source.id", sourceId);
            configuration.put("relation.target.id", targetId);

            instantiate(new InstanceDeclaration(linkId, configuration));

        }

        @Override
        public void unlink(String sourceId, String targetId) {
        	dispose(id(sourceId,targetId));
        }

        @Override
        public boolean isLinked(String sourceId, String targetId) {
        	return get(id(sourceId,targetId)) != null;
        }

        @Override
        public void unlinkOutgoing(String sourceId) {
        	
        	for (String linkId : ids()) {
        		Relation relation = get(linkId).getEntity();
        		
        		if (relation != null && relation.getSource().equals(sourceId)) {
        			dispose(linkId);
        		}
			}
        	
        }

        @Override
        public void unlinkOutgoing(Object source) {
        	unlinkOutgoing(entityId(source));
        	
        }

        @Override
        public void unlinkIncoming(String targetId) {

        	for (String linkId : ids()) {
        		Relation relation = get(linkId).getEntity();
        		
        		if (relation != null && relation.getTarget().equals(targetId)) {
        			dispose(linkId);
        		}
			}
        	
        }

        @Override
        public void unlinkIncoming(Object target) {
        	unlinkIncoming(entityId(target));
        }
        
        @Override
        public void link(Object source, Object target) {
        	link(entityId(source),entityId(target));
        }

        @Override
        public void link(String source, Object target) {
        	link(source,entityId(target));
        }

        @Override
        public void link(Object source, String target) {
        	link(entityId(source),target);
        }

        @Override
        public void unlink(Object source, Object target) {
        	unlink(entityId(source),entityId(target));
        }

        @Override
        public void unlink(String source, Object target) {
        	unlink(source,entityId(target));
        }

        @Override
        public void unlink(Object source, String target) {
        	unlink(entityId(source),target);
        }

        @Override
        public boolean isLinked(Object source, Object target) {
        	return isLinked(entityId(source),entityId(target));
        }

        @Override
        public boolean isLinked(String source, Object target) {
        	return isLinked(source,entityId(target));
        }

        @Override
        public boolean isLinked(Object source, String target) {
        	return isLinked(entityId(source),target);
        }

    }

    private static class EntityCreator extends ComponentCreator implements Creator.Entity<Object> {

        /**
         * The entity created by this factory
         */
        private final String entity;

        protected EntityCreator(String entity) {
            this.entity = entity;
        }

        @Override
        public String getDescription() {
        	return "Entity "+entity;
        }

        @Override
        public Set<String> identifiers() {
        	return new HashSet<>(ids());
        }
        
        @Override
        public boolean shouldBind(ServiceReference<Factory> referenceFactory) {
            String factory = (String) referenceFactory.getProperty("factory.name");
            return factory != null && factory.equals(entity);
        }

        @Override
        public void create(String id, Map<String, Object> initialization) {

            if (get(id) != null) {
                throw new IllegalArgumentException("Entity " + id + " already created");
            }

            int endPackageName = entity.lastIndexOf('.');
            String qualifiedName = (endPackageName != -1 ? entity.substring(0, endPackageName + 1) : "") + id;

            Dictionary<String, Object> configuration = new Hashtable<>();

            configuration.put("instance.name", qualifiedName);
            configuration.put(ContextEntity.CONTEXT_ENTITY_ID, id);

            if (initialization != null) {
                configuration.put("context.entity.init", initialization);
            }

            instantiate(new InstanceDeclaration(id, configuration));
        }

        @Override
        public Object getInstance(String id) {
        	InstanceDeclaration declaration = get(id);
            return declaration != null ? declaration.getEntity() : null;
        }

        @Override
        public String id(Object pojo) {
        	return entityId(pojo);
        }
        
        @Override
        public void delete(String id) {
            dispose(id);
        }


    }

    public class EntityCreatorHandlerDescription extends HandlerDescription {

        private EntityCreatorHandlerDescription() {
            super(CreatorHandler.this);
        }

        @Override
        public synchronized Element getHandlerInfo() {

            Element creatorHandlerDescription = super.getHandlerInfo();

            for (Map.Entry<String, String> injectedField : fieldToContext.entrySet()) {
                String fieldName = injectedField.getValue();
                ComponentCreator creator = creators.get(injectedField.getValue());

                Element creatorElement = new Element("Creator", "");
                creatorElement.addAttribute(new Attribute("field", fieldName));
                creatorElement.addAttribute(new Attribute("context", creator.getDescription()));

                creatorHandlerDescription.addElement(creatorElement);
            }

            return creatorHandlerDescription;
        }
    }
}