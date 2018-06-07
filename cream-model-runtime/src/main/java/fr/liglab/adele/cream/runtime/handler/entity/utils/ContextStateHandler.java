package fr.liglab.adele.cream.runtime.handler.entity.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.util.Property;

import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledFutureTask;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.cream.annotations.internal.ReservedCreamValueReference;
import fr.liglab.adele.cream.model.ContextEntity;



/**
 * 
 * This is the base class that handles mapping of context states to fields in a POJO. 
 * 
 * This class is designed to be shared by Context Entities and Functional Extensions.
 * 
 * Created by aygalinc on 19/07/16.
 */
public abstract class ContextStateHandler extends PrimitiveHandler implements ContextEntity, ContextSource {

    private static final Logger LOG = LoggerFactory.getLogger(ContextStateHandler.class);

    /**
     * The list of exposed context spec
     */
    protected final Set<String> services = new HashSet<>();
    /**
     * The list of states defined in the implemented context spec
     */
    protected final Set<String> stateIds = new HashSet<>();
    /**
     * The list of interceptors in charge of handling each state field
     */
    protected final Set<StateInterceptor> interceptors = new HashSet<>();
    /**
     * The current values of the state properties
     */
    protected final Map<String, Object> stateValues = new ConcurrentHashMap<>();

    /**
     * Get The scheduler
     */
    protected abstract ManagedScheduledExecutorService getScheduler();

    /**
     * The list of iPOJO context listeners to notify on state updates.
     * <p>
     * This handler implements ContextSource to allow state variables to be used in
     * dependency filters.
     */
    private final Map<ContextListener, List<String>> contextSourceListeners = new HashMap<>();
 
    /**
     * Handler Configuration
     **/
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void configure(Element element, Dictionary rawConfiguration, String handlerNameSpace, String handlerName) throws ConfigurationException {


        InstanceManager instanceManager = getInstanceManager();
        String componentName = instanceManager.getInstanceName();

        Dictionary<String, Object> configuration = (Dictionary<String, Object>) rawConfiguration;

        /*
         * Introspect interfaces implemented by the component POJO and construct the
         * state specification of the entity ( basically a set of state variable)
         */
        for (Class<?> service : getInstanceManager().getClazz().getInterfaces()) {
            extractDefinedStatesForService(service);

            /**
             * keeps only exposed context spec
             */
            if (service.isAnnotationPresent(ContextService.class)) {
                services.add(service.getName());
            }
        }

		/*
         * Initialize interceptors for the different field management policies
		 */
        SynchronisationInterceptor synchronisationInterceptor = new SynchronisationInterceptor(this);
        DirectAccessInterceptor directAccessInterceptor = new DirectAccessInterceptor(this);

        interceptors.add(synchronisationInterceptor);
        interceptors.add(directAccessInterceptor);

        /*
         * Parse the manifest and compare if all the state variable declared in the specification are referenced in the implementation.
         * Add the appropriate interceptors to fields and methods
         */

        List<String> implementedStates = new ArrayList<>();
        for (Element entity : optional(element.getElements(handlerName, handlerNameSpace))) {
            for (Element state : optional(entity.getElements("state"))) {

                String stateId = state.getAttribute("id");

                if (stateId == null) {
                    throw new ConfigurationException("Malformed Manifest : a state variable is declared with no 'id' attribute");
                }

                if (!stateIds.contains(stateId)) {
                    throw new ConfigurationException("Malformed Manifest : the state " + stateId + " is not defined in the implemented context spec");
                }

                if (implementedStates.contains(stateId)) {
                    throw new ConfigurationException("Malformed Manifest : several state variable are declared for the same state " + stateId);
                }

                implementedStates.add(stateId);

				/*
				 * Add field and method interceptors according to the specified policy
				 */
                boolean directAccess = Boolean.parseBoolean(state.getAttribute("directAccess"));
                if (!directAccess) {
                    synchronisationInterceptor.configure(state,configuration);
                } else {
                    directAccessInterceptor.configure(state,configuration);
                }

    			/*
    			 * If there is no specified value, but a default was specified for the field, use it
    			 */
                Object configuredValue = getStateConfiguredValue(stateId, configuration);
                if (configuredValue == null) {

                    String defaultValue = state.getAttribute("value");
                    boolean hasDefaultValue = !fr.liglab.adele.cream.annotations.entity.ContextEntity.State.Field.NO_VALUE.equals(defaultValue);
                    if (hasDefaultValue) {
                        configuredValue = defaultValue;
                        setStateConfiguredValue(stateId, defaultValue, configuration);
                    }
                }

                /*
                 * validate configured values are correctly typed for the field
                 */
                String stateField = state.getAttribute("field");
                FieldMetadata fieldMetadata = getPojoMetadata().getField(stateField);
                boolean isValid = configuredValue == null || hasValidType(instanceManager, fieldMetadata, configuredValue);

				/*
				 * If the configured value doesn't have the right type, but it is an String, try to cast it
				 */
                if ((!isValid) && configuredValue != null && (configuredValue instanceof String)) {
                    Object cast = cast(instanceManager, fieldMetadata, (String) configuredValue);
                    if (cast != null) {
                        configuredValue = cast;
                        isValid = true;
                        setStateConfiguredValue(stateId, configuredValue, configuration);
                    }
                }

                if (!isValid) {
                    throw new ConfigurationException("The configured value for state " + stateId + " doesn't match the type of the field :" + configuredValue);
                }
            }
        }

        /*
         * Check that all states defined in the specification are implemented
         */
        Set<String> unimplemented = new HashSet<>(stateIds);
        unimplemented.removeAll(implementedStates);

        if (!unimplemented.isEmpty()) {
            throw new ConfigurationException("States " + unimplemented + " are defined in the context service, but never implemented in " + componentName);
        }

    	/*
         * Check the context entity id was specified
         */
        if (configuration.get(CONTEXT_ENTITY_ID) == null) {
            throw new ConfigurationException("Try to instantiate a context entity without and context.entity.id element");
        }
        stateIds.add(CONTEXT_ENTITY_ID);
        update(CONTEXT_ENTITY_ID, configuration.get(CONTEXT_ENTITY_ID));

        /*
         * Initialize the state map with the configured values
         */
        for (String configuredState : getConfiguredStates(configuration)) {
            if (stateIds.contains(configuredState)) {
                update(configuredState, getStateConfiguredValue(configuredState, configuration));
            } else {
                debug("Configured state " + configuredState + " will be ignored, it is not defined in the context spec of " + componentName);
            }
        }
    }

    @Override
    public void reconfigure(Dictionary rawConfiguration) {
       
    	@SuppressWarnings("unchecked")
    	Dictionary<String,Object> configuration = (Dictionary<String, Object>) rawConfiguration;
        
        for (StateInterceptor interceptor : interceptors) {
            interceptor.reconfigure(configuration);
        }
    }

    /**
     * HandlerLifecycle
     */
    
    @Override
    public synchronized void start() {
    }

    @Override
    public synchronized void stop() {
    }

    public synchronized void stateChanged(int state) {

        if (state == InstanceManager.VALID) {
  
            /*
             * restart state handlers
             */
            for (StateInterceptor interceptor : interceptors) {
                interceptor.validate();
            }
        }

        if (state == InstanceManager.INVALID) {
              /*
             * stop state handlers
             */
            for (StateInterceptor interceptor : interceptors) {
                interceptor.invalidate();
            }
        }
    }

    /**
     * Updates the value of a state property, propagating the change to the published service properties
     *
     * @param stateId
     * @param value
     */
    protected boolean update(String stateId, Object value) {

        if (stateId == null && !stateIds.contains(stateId)) {
            return false;
        }

        Object oldValue = stateValues.get(stateId);
        boolean bothNull = oldValue == null && value == null;
        boolean equals = (oldValue != null && value != null) && oldValue.equals(value);
        boolean noChange = bothNull || equals;


        if (noChange) {
            return false;
        }

        if (value != null) {
            stateValues.put(stateId, value);
        } else {
            stateValues.remove(stateId);
        }

        if (CONTEXT_ENTITY_ID.equals(stateId)) {
            return true;
        }

        notifyContextListener(stateId, value);
        return true;
    }

    /**
     * Get the definition of the states associated to a given context service
     */
    private void extractDefinedStatesForService(Class<?> service) {

    	/*
    	 * consider only context spec
    	 */
        if (!service.isAnnotationPresent(ContextService.class)) {
            return;
        }

        String contextServiceName = service.getAnnotation(ContextService.class).value();
        if (contextServiceName.equals(ContextService.DEFAULT_VALUE)) {
            contextServiceName = service.getSimpleName().toLowerCase();
        }

    	/*
    	 * look for all states defined in the context service interface.
    	 *
    	 * The states of a service are defined by static, String fields marked with the annotation State.
    	 * The value of the field is the name of the state.
    	 *
    	 */
        for (Field field : service.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(String.class) && field.isAnnotationPresent(State.class)) {
                try {
                    String stateName = String.class.cast(field.get(null));
                    stateIds.add(contextServiceName + "." + stateName);
                } catch (IllegalArgumentException | IllegalAccessException ignored) {
                    LOG.info("Ignored exception", ignored);
                }

            }
        }
        for (Class<?> inheritedService : service.getInterfaces()) {
            extractDefinedStatesForService(inheritedService);
        }
    }

    /**
     * Context Entity Implementation
     */

    @Override
    public Set<String> getServices() {
        return services;
    }


    @Override
    public Set<String> getStates() {
        return new HashSet<>(stateIds);
    }

    @Override
    public Object getValue(String state) {
        return state != null ? stateValues.get(state) : null;
    }

    @Override
    public Map<String, Object> getValues() {
        return new HashMap<>(stateValues);
    }

    /**
     * Context Source Implementation.
     * 
     * Implementation must be very defensive because this method can be called even if the instance manager
     * is not yet attached
     */

    @Override
    public Object getProperty(String property) {
        return stateValues.get(property);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Dictionary getContext() {
        return new Hashtable<>(stateValues);
    }

    @Override
    public void registerContextListener(ContextListener listener, String[] properties) {

    	boolean isRegistered 				= contextSourceListeners.containsKey(listener);
    	List<String> registeredProperties 	= contextSourceListeners.get(listener);
    	
    	if (isRegistered && registeredProperties != null && properties != null) {
    		registeredProperties.addAll(Arrays.asList(properties));
    	}

    	if (isRegistered && registeredProperties != null && properties == null) {
    		registeredProperties = null;
    		contextSourceListeners.put(listener,registeredProperties);
    	}

    	if (! isRegistered) {
    		registeredProperties = properties != null ? Arrays.asList(properties) : null;
    		contextSourceListeners.put(listener,registeredProperties);
    	}
    	
    	for (Map.Entry<String,Object> state : stateValues.entrySet()) {
			if (registeredProperties == null || registeredProperties.contains(state.getKey())) {
				listener.update(this, state.getKey(), state.getValue());
			}
		}
    	
    }

    @Override
    public synchronized void unregisterContextListener(ContextListener listener) {
        contextSourceListeners.remove(listener);
    }

    /**
     * Notify All the context listener
     */
    protected void notifyContextListener(String property, Object value) {
        for (Map.Entry<ContextListener, List<String>> listener : contextSourceListeners.entrySet()) {
            if (listener.getValue() == null || listener.getValue().contains(property)) {
                listener.getKey().update(this, property, value);
            }
        }
    }

    @Override
    public HandlerDescription getDescription() {
        return new EntityHandlerDescription();
    }

    /**
     * Add a new periodic task that will be executed periodically, this is useful for automating state
     * updates.
     * <p>
     * The action to be performed is specified as a consumer that will be given access to the component
     * instance and the state
     */
    public PeriodicTask schedule(String stateId, BiConsumer<InstanceManager,String> action, long period, TimeUnit unit) {
       return new PeriodicTask(stateId, action, period, unit);
    }

    /**
     * The description of the handler.
     * <p>
     * This class exposes the generic interface ContextEntity to allow external code to introspect the
     * component instance and obtain the current state values.
     */
    public class EntityHandlerDescription extends HandlerDescription implements ContextEntity {

        private EntityHandlerDescription() {
            super(ContextStateHandler.this);
        }

        @Override
        public Set<String> getServices() {
            return ContextStateHandler.this.getServices();
        }

        @Override
        public Set<String> getStates() {
            return ContextStateHandler.this.getStates();
        }

        @Override
        public Map<String, Object> getValues() {
            return ContextStateHandler.this.getValues();
        }

        @Override
        public Object getValue(String getStateValue) {
            return ContextStateHandler.this.getValue(getStateValue);
        }

       @Override
        public Element getHandlerInfo() {
            Element handlerInfo = super.getHandlerInfo();
           
            String specifications = getServices().stream().collect(Collectors.joining(",", "{", "}"));
            handlerInfo.addAttribute(new Attribute("context.specifications",specifications));

            for ( String stateId : getStates()) {
            	
                Element stateElement = new Element("state", null);
                stateElement.addAttribute(new Attribute("id", stateId));
                
                Map<String,Object> val = getValues();
                
                if (val.containsKey(stateId)){
                    stateElement.addAttribute(new Attribute("value", val.get(stateId).toString()));
                } else {
                    stateElement.addAttribute(new Attribute("value", ReservedCreamValueReference.NOT_VALUED_STATES.toString()));
                }
                
                for (StateInterceptor interceptor : interceptors){
                    interceptor.getInterceptorInfo(stateId, stateElement);
                }
                
                handlerInfo.addElement(stateElement);
            }

            return handlerInfo;
        }
    }

    /**
     * This class keeps track of all the information required to schedule periodic tasks
     */
    public final class PeriodicTask {


        private final String stateId;

    	private final BiConsumer<InstanceManager,String> action;

        private long period;

        private TimeUnit unit;

        private ManagedScheduledFutureTask<?> taskHandle;

        private PeriodicTask(String stateId, BiConsumer<InstanceManager,String> action, long period, TimeUnit unit) {
            
        	this.stateId	= stateId;
            this.action 	= action;

            this.period 	= period;
            this.unit 		= unit;
        }

        private final void trigger() {
        	
        	InstanceManager instance = getInstanceManager();
            
        	if (instance.getState() == ComponentInstance.VALID) {
                action.accept(getInstanceManager(),stateId);
            }
        }

        /**
         * Start executing the action periodically
         */
        public synchronized void start() {

			/*
			 * Handle non periodic tasks, as a single shot activation when started
			 */
            if (period < 0) {
            	trigger();
            } 
            
            if (period > 0 && taskHandle == null) {
                taskHandle = getScheduler().scheduleAtFixedRate(this::trigger, period, period, unit);
            }
        }

        /**
         * Stop executing the action
         */
        public synchronized void stop() {
            if (taskHandle != null) {
                taskHandle.cancel(true);
                taskHandle = null;
            }
        }

        public synchronized boolean isStarted() {
        	return taskHandle != null;
        }
        
		public synchronized void reconfigure(long period, TimeUnit unit) {

			boolean wasStarted = isStarted();
			
			stop();
			
			this.period = period;
			this.unit	= unit;
			
			if (wasStarted) {
				start();
			}
		}

        public long getPeriod() {
            return period;
        }

        public TimeUnit getUnit() {
            return unit;
        }


    }

    /**
     * Utility function to handle optional configuration
     */
    private static final Element[] EMPTY_OPTIONAL = new Element[0];

    protected static final Element[] optional(Element[] elements) {
        return elements != null ? elements : EMPTY_OPTIONAL;
    }

    /**
     * Get the list of configured states of the instance
     */
    protected static final Set<String> getConfiguredStates(Dictionary<String, ?> configuration) {
        @SuppressWarnings("unchecked")
        Map<String, Object> stateConfiguration = (Map<String, Object>) configuration.get("context.entity.init");

        if (stateConfiguration == null)
            return Collections.emptySet();

        return stateConfiguration.keySet();
    }

    /**
     * Get the value of a state defined in the instance configuration
     */
    protected static final Object getStateConfiguredValue(String stateId, Dictionary<String, ?> configuration) {

        @SuppressWarnings("unchecked")
        Map<String, Object> stateConfiguration = (Map<String, Object>) configuration.get("context.entity.init");

        if (stateConfiguration == null)
            return null;

        return stateConfiguration.get(stateId);
    }

    /**
     * Set the value of a state  in the instance configuration
     */
    protected static final void setStateConfiguredValue(String stateId, Object value, Dictionary<String, Object> configuration) {

        @SuppressWarnings("unchecked")
        Map<String, Object> stateConfiguration = (Map<String, Object>) configuration.get("context.entity.init");

        if (stateConfiguration == null) {
            stateConfiguration = new HashMap<>();
            configuration.put("context.entity.init", stateConfiguration);
        }

        stateConfiguration.put(stateId, value);
    }

    /**
     * Cast a string value to the type of the specified field
     */
    protected static final Object cast(InstanceManager component, FieldMetadata field, String value) {
        try {
            Class<?> type = Property.computeType(field.getFieldType(), component.getGlobalContext());
            return Property.create(type, value);
        } catch (ConfigurationException ignored) {
            LOG.info("Ignored exception ", ignored);
            return null;
        }
    }

    /**
     * Verify the type of the specified value matches the type of field
     */
    protected static final boolean hasValidType(InstanceManager component, FieldMetadata field, Object value) {
        try {
            Class<?> type = boxed(Property.computeType(field.getFieldType(), component.getGlobalContext()));
            return type.isInstance(value);
        } catch (ConfigurationException ignored) {
            LOG.info("Ignored exception ", ignored);
            return false;
        }
    }

    /**
     * Get a boxed class that can be used to determine if an object reference is instance of the given class,
     * even in the case of primitive types.
     * <p>
     * NOTE notice that Class.isInstance always returns false for primitive types. So we need to use the appropiate
     * wrapper when testing for asignment comptibility.
     */
    protected static final Class<?> boxed(@SuppressWarnings("rawtypes") Class type) {
        if (!type.isPrimitive())
            return type;

        if (type == Boolean.TYPE)
            return Boolean.class;
        if (type == Character.TYPE)
            return Character.class;
        if (type == Byte.TYPE)
            return Byte.class;
        if (type == Short.TYPE)
            return Short.class;
        if (type == Integer.TYPE)
            return Integer.class;
        if (type == Long.TYPE)
            return Long.class;
        if (type == Float.TYPE)
            return Float.class;
        if (type == Double.TYPE)
            return Double.class;
        // void.class remains
        throw new IllegalArgumentException(type + " is not permitted");
    }


}
