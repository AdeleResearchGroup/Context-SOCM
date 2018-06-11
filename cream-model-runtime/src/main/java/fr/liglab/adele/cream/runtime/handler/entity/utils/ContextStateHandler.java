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
public abstract class ContextStateHandler extends PrimitiveHandler implements ContextSource, ContextEntity {

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
     * The initial configured values of the state properties
     */
    protected final Map<String, Object> configuredValues = new HashMap<>();

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

        Dictionary<String, Object> configuration = (Dictionary<String, Object>) rawConfiguration;

        /*
         * Introspect interfaces implemented by the component POJO and construct the
         * state specification of the entity ( basically a set of state variable)
         */
        for (Class<?> service : getInstanceManager().getClazz().getInterfaces()) {

            /**
             * keeps only exposed context spec
             */
            if (service.isAnnotationPresent(ContextService.class)) {
                services.add(service.getName());
            }

            stateIds.addAll(getDeclaredStates(service));

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
    			 * Get the initial configured value of the state
    			 */
                Object configuredValue = getConfiguredValue(stateId, configuration, state);
                if (configuredValue != null) {
                	configuredValues.put(stateId,configuredValue);
                }
                
            }
        }

        /*
         * Check that all states defined in the specification are implemented
         */
        Set<String> unimplemented = new HashSet<>(stateIds);
        unimplemented.removeAll(implementedStates);

        if (!unimplemented.isEmpty()) {
            throw new ConfigurationException("States " + unimplemented + " are defined in the context service, but never implemented in " + getInstanceManager().getInstanceName());
        }

    	/*
         * Check the context entity id was specified
         */
        if (configuration.get(ContextEntity.CONTEXT_ENTITY_ID) == null) {
            throw new ConfigurationException("Try to instantiate a context entity without and context.entity.id element");
        }
        
        stateIds.add(ContextEntity.CONTEXT_ENTITY_ID);
        configuredValues.put(ContextEntity.CONTEXT_ENTITY_ID,configuration.get(ContextEntity.CONTEXT_ENTITY_ID));

        /*
         * Initialize the state map with the configured values
         */
        reset();
    }

    @Override
    public void reconfigure(Dictionary rawConfiguration) {
       
    	@SuppressWarnings("unchecked")
    	Dictionary<String,Object> configuration = (Dictionary<String, Object>) rawConfiguration;
        
        for (StateInterceptor interceptor : interceptors) {
            interceptor.reconfigure(configuration);
        }
    }

    private void reset() {
    	
    	for (String state : stateIds) {
       		update(state,configuredValues.get(state));
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
    	reset();
    }

    public synchronized void stateChanged(int componentState) {

        if (componentState == InstanceManager.VALID) {
  
            /*
             * restart state handlers
             */
            for (StateInterceptor interceptor : interceptors) {
                interceptor.validate();
            }
            
        }

        if (componentState == InstanceManager.INVALID) {
        	

            /*
             * stop state handlers
             */
            for (StateInterceptor interceptor : interceptors) {
                interceptor.invalidate();
            }
            
        }
        
    }

    /**
     * Updates the value of a state property, notifying the context listeners
     */
    protected void update(String stateId, Object value) {

        if (stateId == null && !stateIds.contains(stateId)) {
            return;
        }

        Object oldValue = stateValues.get(stateId);
        
        boolean bothNull = oldValue == null && value == null;
        boolean equals = (oldValue != null && value != null) && oldValue.equals(value);

        if (bothNull || equals) {
            return;
        }

        if (value != null) {
            stateValues.put(stateId, value);
        } else {
            stateValues.remove(stateId);
        }

        notifyContextListeners(stateId, oldValue, value);
    }

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
     * Get the definition of the states associated to a given context service
     */
    private List<String> getDeclaredStates(Class<?> service) {

        List<String> result = new ArrayList<>();

    	/*
    	 * consider only states defined in context services
    	 */
        if (service.isAnnotationPresent(ContextService.class)) {
        	
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
	                    result.add(contextServiceName + "." + stateName);
	                } catch (IllegalArgumentException | IllegalAccessException ignored) {
	                    LOG.info("Ignored exception", ignored);
	                }
	            }
	        }
        }
        
        /*
         * Add inherited states
         */
        for (Class<?> inheritedService : service.getInterfaces()) {
            result.addAll(getDeclaredStates(inheritedService));
        }
        
        return result;
    }

    /**
     * Context Source Implementation.
     * 
     * Implementation must be very defensive because this method can be called even if the instance manager
     * is not yet attached
     */

    @Override
    public Object getProperty(String property) {
        return getValue(property);
    }

    @Override
    public Dictionary getContext() {
        return new Hashtable<>(getValues());
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
     * Notify All the context listeners of a state change
     */
    protected void notifyContextListeners(String property, Object oldValue, Object value) {
    	
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
     * component instance description and obtain the current state values.
     */
    public class EntityHandlerDescription extends HandlerDescription implements ContextEntity {

        private EntityHandlerDescription() {
            super(ContextStateHandler.this);
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


		@Override
		public Set<String> getServices() {
			return ContextStateHandler.this.getServices();
		}
	
	
		@Override
		public Object getValue(String state) {
			return ContextStateHandler.this.getValue(state);
		}
	
	
		@Override
		public Set<String> getStates() {
			return ContextStateHandler.this.getStates();
		}
	
	
		@Override
		public Map<String, Object> getValues() {
			return ContextStateHandler.this.getValues();
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
        public void start() {

			/*
			 * Handle non periodic tasks, as a single shot activation when started
			 */
            if (period < 0) {
            	trigger();
            } 
            
            synchronized(this) {
                if (period > 0 && taskHandle == null) {
                    taskHandle = getScheduler().scheduleAtFixedRate(this::trigger, period, period, unit);
                }
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


    private Object getConfiguredValue(String state, Dictionary<String,Object> configuration, Element stateDeclaration) throws ConfigurationException {
    	
        Map<String, Object> stateConfiguration	= (Map<String,Object>) configuration.get("context.entity.init");
        Object configuredValue  				= stateConfiguration != null ? stateConfiguration.get(state) : null;

        if (configuredValue == null) {

            String defaultValue = stateDeclaration.getAttribute("value");
            boolean hasDefaultValue = !fr.liglab.adele.cream.annotations.entity.ContextEntity.State.Field.NO_VALUE.equals(defaultValue);
            
            if (hasDefaultValue) {
                configuredValue = defaultValue;
            }
        }

        /*
         * validate configured values are correctly typed for the field
         */
        String stateField = stateDeclaration.getAttribute("field");
        FieldMetadata fieldMetadata = getPojoMetadata().getField(stateField);
        
        boolean isValid = configuredValue == null || isInstance(fieldMetadata,configuredValue);

		/*
		 * If the configured value doesn't have the right type, but it is an String, try to cast it
		 */
        if ((!isValid) && configuredValue != null && (configuredValue instanceof String)) {
            Object cast = cast(fieldMetadata, (String) configuredValue);
            if (cast != null) {
                configuredValue = cast;
                isValid = true;
            }
        }

        if (!isValid) {
            throw new ConfigurationException("The configured value for state " + state + " doesn't match the type of the field :" + configuredValue);
        }
    	
        return configuredValue;
    }
    

    /**
     * The type of a field of the component
     */
    protected final Class<?> type(FieldMetadata field) throws ConfigurationException {
    	 return Property.computeType(field.getFieldType(), getInstanceManager().getGlobalContext());
    }
    
    /**
     * Cast a string value to the type of the specified field
     */
    protected final Object cast(FieldMetadata field, String value) {
        try {
            return Property.create(type(field), value);
        } catch (ConfigurationException ignored) {
            LOG.info("Ignored exception ", ignored);
            return null;
        }
    }

    /**
     * Verify the type of the specified value matches the type of field
     */
    protected final boolean isInstance(FieldMetadata field, Object value) {
        try {
            return boxed(type(field)).isInstance(value);
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
