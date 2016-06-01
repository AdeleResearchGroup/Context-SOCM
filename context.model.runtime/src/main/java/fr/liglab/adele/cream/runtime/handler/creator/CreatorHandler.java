package fr.liglab.adele.cream.runtime.handler.creator;

import fr.liglab.adele.cream.runtime.RelationImpl;
import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.model.Relation;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.cream.runtime.handler.entity.EntityHandler;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.Pojo;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.osgi.framework.ServiceReference;

import java.util.*;
import java.util.stream.Collectors;

@Handler(name = HandlerReference.CREATOR_HANDLER, namespace = HandlerReference.NAMESPACE)
@Provides(specifications = {EntityProvider.class, RelationProvider.class})

public class CreatorHandler extends PrimitiveHandler implements EntityProvider, RelationProvider {

	private final Map<String,String> fieldToContext		= new HashMap<>();

	private final Map<String,ComponentCreator> creators 	= new HashMap<>();

	@Override
	public void configure(Element metadata, @SuppressWarnings("rawtypes") Dictionary configuration) throws ConfigurationException {

		InstanceManager instanceManager = getInstanceManager();

		Element[] creatorElements = metadata.getElements(HandlerReference.CREATOR_HANDLER, HandlerReference.NAMESPACE);

		for (Element creator: creatorElements) {


			String fieldName		= creator.getAttribute("field");
			String componentName	= instanceManager.getClassName();

			FieldMetadata field		 = getPojoMetadata().getField(fieldName);

			if (field == null) {
				throw new ConfigurationException("Malformed Manifest : the specified creator field '"+fieldName+"' is not defined in class "+componentName);
			}

			String entity 				= creator.getAttribute("entity");
			String relation				= creator.getAttribute("relation");

			if (entity == null && relation == null) {
				throw new ConfigurationException("Malformed Manifest : the creator entity or relation is not specified for field '"+fieldName+"' in class "+componentName);
			}

			if (entity != null && relation == null) {
				instantiateEntityCreator(entity);
				fieldToContext.put(fieldName,entity);
			}

			if (entity != null && relation != null) {
				instantiateRelationCreator(relation);
				fieldToContext.put(fieldName,relation);
			}

			instanceManager.register(getPojoMetadata().getField(fieldName),this);
		}
	}

	/**
	 * Instantiate, if necessary, the creator associated with a given entity
	 */
	private void instantiateEntityCreator(String entity) {

		ComponentCreator creator = creators.get(entity);
		if (creator == null) {
			creator = new EntityCreator(entity);
			creators.put(entity,creator);
		}
	}

	/**
	 *Instantiate, if necessary, the creator associated with a given relation
	 */
	private void instantiateRelationCreator(String relation) {

		ComponentCreator creator = creators.get(relation);
		if (creator == null) {
			creator = new RelationCreator(relation);
			creators.put(relation,creator);
		}
	}

	@Override
	public Object onGet(Object pojo, String fieldName, Object value){
		return creators.get(fieldToContext.get(fieldName));
	}

	@Override
	public void onSet(Object pojo, String fieldName, Object value) {
	}

	/**
	 * Binds an iPOJO factory, it verifies that the factory is not required by any
	 */
	@Bind(id="ipojo.factory", aggregate=true, proxy=false, optional=true)
	public void bindFactory(Factory factory, ServiceReference<Factory> reference) {
		for (ComponentCreator creator : creators.values()) {
			if (creator.shouldBind(reference)) {
				creator.bindFactory(factory);
			}
		}
	}

	@Unbind(id="ipojo.factory")
	public void unbindFactory(Factory factory, ServiceReference<Factory> reference) {
		for (ComponentCreator creator : creators.values()) {
			if (creator.shouldBind(reference)) {
				creator.unbindFactory();
			}
		}
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
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
		if (creators.get(contextItem) == null) {
			return false;
		}

		return creators.get(contextItem) != null ? creators.get(contextItem).isEnabled(): false;
	}

	@Override
	public boolean enable(String contextItem) {
		if (creators.get(contextItem) == null) {
			return false;
		}

		return creators.get(contextItem).setEnabled(true);
	}

	@Override
	public boolean disable(String contextItem) {
		if (creators.get(contextItem) == null) {
			return false;
		}

		return creators.get(contextItem).setEnabled(false);
	}

	@Override
	public Set<String> getInstances(String contextItem, boolean includePending) {
		if (creators.get(contextItem) == null) {
			return Collections.emptySet();
		}

		return creators.get(contextItem).getInstanceDeclarations(instance -> includePending || instance.isInstantiated())
				.map(InstanceDeclaration::getName)
				.collect(Collectors.toSet());
	}

	@Override
	public boolean deleteInstances(String contextItem, boolean onlyPending) {

		if (creators.get(contextItem) == null) {
			return false;
		}

		Set<String> instances = creators.get(contextItem).getInstanceDeclarations(instance -> (!onlyPending) || !instance.isInstantiated())
				.map(InstanceDeclaration::getName)
				.collect(Collectors.toSet());

		for (String instance : instances) {
			creators.get(contextItem).delete(instance);
		}

		return true;
	}

	private static class RelationCreator extends ComponentCreator implements Creator.Relation<Object,Object> {

		/**
		 * The relation created by this factory
		 */
		private final String relation;

		protected RelationCreator(String relation) {
			super("Relation"+relation);
			this.relation = relation;
		}

		@Override
		public boolean shouldBind(ServiceReference<Factory> referenceFactory) {
			String factory = (String) referenceFactory.getProperty("factory.name");
			return factory != null && factory.equals(RelationImpl.class.getName());
		}

		private final String id(String sourceId, String targetId) {
			return relation+"["+sourceId+"-"+targetId+"]";
		}

		@Override
		public String create(String sourceId, String targetId) {

			String id	= id(sourceId,targetId);
			if (this.instances.containsKey(id)) {
				throw new IllegalArgumentException("Relation "+relation+" from "+sourceId+" to "+targetId+" already created");
			}

			Dictionary<String, Object> configuration = new Hashtable<>();

			configuration.put("instance.name",id);
			configuration.put("relation.id",relation);
			configuration.put("relation.source.id",sourceId);
			configuration.put("relation.target.id",targetId);

			super.create(new InstanceDeclaration(id,configuration));

			return id;
		}

		@Override
		public String create(Object source, Object target) {
			if ((source instanceof Pojo) && (target instanceof Pojo)) {
				return create(EntityHandler.getContextEntity((Pojo)source).getId(),EntityHandler.getContextEntity((Pojo)target).getId());
			}
			
			throw new IllegalArgumentException("source or target object is not a context entity");
		}
		
		@Override
		public String create(Object source, String targetId) {
			if ((source instanceof Pojo) && (targetId != null)) {
				return create(EntityHandler.getContextEntity((Pojo)source).getId(),targetId);
			}
			
			throw new IllegalArgumentException("source or target object is not a context entity");
		}

		@Override
		public String create(String sourceId, Object target) {
			if ((sourceId != null) && (target instanceof Pojo)) {
				return create(sourceId,EntityHandler.getContextEntity((Pojo)target).getId());
			}
			
			throw new IllegalArgumentException("source or target object is not a context entity");
		}
		
		
		@Override
		public Set<String> getInstances() {
			return getInstanceDeclarations()
					.map(InstanceDeclaration::getName)
					.collect(Collectors.toSet());
		}

		@Override
		public Relation getInstance(String id) {
			if (id == null)
				return null;
			
			InstanceDeclaration declaration = this.instances.get(id);
			if (declaration == null) {
				return null;
			}

			if (declaration.instance == null) {
				return null;
			}

			if (! (declaration.instance instanceof InstanceManager)) {
				return null;
			}

			return (Relation)((InstanceManager)declaration.instance).getPojoObject();
		}

		@Override
		public List<Relation> getInstancesRelatedTo(String sourceId) {
			List<Relation> relations = new ArrayList<>();
			for (String relationId : this.instances.keySet()) {
				Relation relation = getInstance(relationId);
				if (relation != null && relation.getSource().equals(sourceId)) {
					relations.add(relation);
				}
			}
			return relations;
		}

		@Override
		public List<Relation> getInstancesRelatedTo(Object source) {
			if (source instanceof Pojo) {
				return getInstancesRelatedTo(EntityHandler.getContextEntity((Pojo)source).getId());
			}
			
			throw new IllegalArgumentException("source object is not a context entity");
		}

		@Override
		public void delete(String id) {
			super.delete(id);
		}

		@Override
		public void delete(String sourceId, String targetId) {
			delete(id(sourceId,targetId));
		}

		@Override
		public void delete(Object source, Object target) {
			if ((source instanceof Pojo) && (target instanceof Pojo)) {
				delete(EntityHandler.getContextEntity((Pojo)source).getId(),EntityHandler.getContextEntity((Pojo)target).getId());
			}
		}

		@Override
		public void delete(Object source, String targetId) {
			if ((source instanceof Pojo) && (targetId != null)) {
				delete(EntityHandler.getContextEntity((Pojo)source).getId(),targetId);
			}
		}

		@Override
		public void delete(String sourceId, Object target) {
			if ((sourceId != null) && (target instanceof Pojo)) {
				delete(sourceId,EntityHandler.getContextEntity((Pojo)target).getId());
			}
		}
		
		@Override
		public void deleteAll() {
			Set<String> instances = getInstances();
			for (String instance : instances) {
				delete(instance);
			}
		}

	}

	private static class EntityCreator extends ComponentCreator implements Creator.Entity<Object> {

		/**
		 * The entity created by this factory
		 */
		private final String entity;

		protected EntityCreator(String entity){
			super("Entity "+entity);

			this.entity = entity;
		}

		@Override
		public boolean shouldBind(ServiceReference<Factory> referenceFactory) {
			String factory = (String) referenceFactory.getProperty("factory.name");
			return factory != null && factory.equals(entity);
		}

		@Override
		public void create(String id) {
			create(id,null);
		}

		@Override
		public void create(String id, Map<String, Object> initialization) {

			if (this.instances.containsKey(id)) {
				throw new IllegalArgumentException("Entity "+id+" already created");
			}

			int endPackageName = entity.lastIndexOf('.');
			String qualifiedName = (endPackageName != -1 ? entity.substring(0, endPackageName+1) : "")+id;

			Dictionary<String, Object> configuration = new Hashtable<String,Object>();

			configuration.put("instance.name",qualifiedName);
			configuration.put(ContextEntity.CONTEXT_ENTITY_ID,id);

			if (initialization != null) {
				configuration.put("context.entity.init",initialization);
			}

			super.create(new InstanceDeclaration(id,configuration) {

				@Override
				public void dispose() {

					/**
					 * For entities we try to save the state of the entity before disposing it
					 */
					ContextEntity entity = EntityHandler.getContextEntity(this.instance);
					if (entity != null) {
						configuration.put("context.entity.init",entity.dumpState());
					}

					super.dispose();
				}

			});
		}

		@Override
		public void delete(String id) {
			super.delete(id);
		}

		@Override
		public Object getInstance(String id) {
			InstanceDeclaration declaration = this.instances.get(id);
			if (declaration == null) {
				return null;
			}

			if (declaration.instance == null) {
				return null;
			}

			if (! (declaration.instance instanceof InstanceManager)) {
				return null;
			}

			return ((InstanceManager)declaration.instance).getPojoObject();
		}

		@Override
		public Set<String> getInstances() {
			return getInstanceDeclarations()
					.map(InstanceDeclaration::getName)
					.collect(Collectors.toSet());
		}

		@Override
		public void deleteAll() {
			Set<String> instances = getInstances();
			for (String instance : instances) {
				delete(instance);
			}
		}

	}

	@Override
	public HandlerDescription getDescription() {
		return new EntityCreatorHandlerDescription();
	}


	public class EntityCreatorHandlerDescription extends HandlerDescription {

		private EntityCreatorHandlerDescription() {
			super(CreatorHandler.this);
		}

		public synchronized Element getHandlerInfo() {

			Element creatorHandlerDescription = super.getHandlerInfo();

			for (Map.Entry<String,String> injectedField : fieldToContext.entrySet()) {
				String fieldName 		= injectedField.getValue();
				ComponentCreator creator	= creators.get(injectedField.getValue());

				Element creatorElement = new Element("Creator","");
				creatorElement.addAttribute(new Attribute("field",fieldName));
				creatorElement.addAttribute(new Attribute("context", creator.getDescription()));

				creatorHandlerDescription.addElement(creatorElement);
			}

			return creatorHandlerDescription;
		}
	}

}