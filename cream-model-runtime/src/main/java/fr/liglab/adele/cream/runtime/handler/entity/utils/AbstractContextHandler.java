package fr.liglab.adele.cream.runtime.handler.entity.utils;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.model.ContextEntity;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.util.Property;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledFutureTask;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by aygalinc on 19/07/16.
 */
public abstract class AbstractContextHandler extends PrimitiveHandler implements ContextEntity {


    /**
     * The list of exposed context services
     */
    protected final Set<String> services 					= new HashSet<>();

    /**
     * The list of states defined in the implemented context services
     */
    protected final Set<String> stateIds 					= new HashSet<>();

    /**
     * The list of interceptors in charge of handling each state field
     */
    protected final Set<StateInterceptor>	interceptors	= new HashSet<>();


    /**
     * Get The scheduler
     */
    abstract protected ManagedScheduledExecutorService getScheduler();

    /**
     * Is the instance active
     */
    abstract protected boolean isInstanceActive();

    /**
     * Updates the value of a state property, propagating the change to the published service properties
     */
    abstract public void update(String stateId, Object value);



    /**
     * Handler Configuration
     *
     **/
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void configure(Element element, Dictionary rawConfiguration,String handlerNameSpace,String handlerName) throws ConfigurationException {


        InstanceManager instanceManager 		= getInstanceManager();
        String componentName					= instanceManager.getInstanceName();

        Dictionary<String,Object> configuration	= (Dictionary<String,Object>) rawConfiguration;

        /*
         * Introspect interfaces implemented by the component POJO and construct the
         * state specification of the entity ( basically a set of state variable)
         */
        for(Class<?> service : getInstanceManager().getClazz().getInterfaces()) {
            extractDefinedStatesForService(service);

            /**
             * keeps only exposed context services
             */
            if (service.isAnnotationPresent(ContextService.class)) {
                services.add(service.getName());
            }
        }

		/*
		 * Initialize interceptors for the different field management policies
		 */
        SynchronisationInterceptor synchronisationInterceptor 	= new SynchronisationInterceptor(this);
        DirectAccessInterceptor directAccessInterceptor 		= new DirectAccessInterceptor(this);

        interceptors.add(synchronisationInterceptor);
        interceptors.add(directAccessInterceptor);

        /*
         * Parse the manifest and compare if all the state variable declared in the specification are referenced in the implementation.
         * Add the appropriate interceptors to fields and methods
         */

        List<String> implementedStates = new ArrayList<String>();
        for (Element entity : optional(element.getElements(handlerName,handlerNameSpace))) {
            for (Element state : optional(entity.getElements("state"))) {

                String stateId 			= state.getAttribute("id");

                if (stateId == null) {
                    throw new ConfigurationException("Malformed Manifest : a state variable is declared with no 'id' attribute");
                }

                if (! stateIds.contains(stateId)) {
                    throw new ConfigurationException("Malformed Manifest : the state "+stateId+" is not defined in the implemented context services");
                }

                if (implementedStates.contains(stateId)) {
                    throw new ConfigurationException("Malformed Manifest : several state variable are declared for the same state "+stateId);
                }

                implementedStates.add(stateId);

				/*
				 * Add field and method interceptors according to the specified policy
				 */
                boolean directAccess	= Boolean.valueOf(state.getAttribute("directAccess"));
                if (!directAccess) {
                    synchronisationInterceptor.handleState(instanceManager,getPojoMetadata(),state);
                }

                else {
                    directAccessInterceptor.handleState(instanceManager,getPojoMetadata(),state);
                }

    			/*
    			 * If there is no specified value, but a default was specified for the field, use it
    			 */
                Object configuredValue 	= getStateConfiguredValue(stateId,configuration);
                if (configuredValue == null) {

                    String defaultValue 	= state.getAttribute("value");
                    boolean hasDefaultValue	=  !fr.liglab.adele.cream.annotations.entity.ContextEntity.State.Field.NO_VALUE.equals(defaultValue);
                    if (hasDefaultValue) {
                        configuredValue = defaultValue;
                        setStateConfiguredValue(stateId,defaultValue,configuration);
                    }
                }

                /*
                 * validate configured values are correctly typed for the field
                 */
                String stateField			= state.getAttribute("field");
                FieldMetadata fieldMetadata = getPojoMetadata().getField(stateField);
                boolean isValid 			= configuredValue == null || hasValidType(instanceManager,fieldMetadata,configuredValue);

				/*
				 * If the configured value doesn't have the right type, but it is an String, try to cast it
				 */
                if ( (!isValid) && configuredValue != null && (configuredValue instanceof String)) {
                    Object cast = cast(instanceManager,fieldMetadata,(String)configuredValue);
                    if (cast != null) {
                        configuredValue = cast;
                        isValid			= true;
                        setStateConfiguredValue(stateId, configuredValue, configuration);
                    }
                }

                if (! isValid) {
                    throw new ConfigurationException("The configured value for state "+stateId+" doesn't match the type of the field :"+ configuredValue);
                }
            }
        }

        /*
         * Check that all states defined in the specification are implemented
         */
        Set<String> unimplemented = new HashSet<>(stateIds);
        unimplemented.removeAll(implementedStates);

        if (! unimplemented.isEmpty()) {
            throw new ConfigurationException("States " + unimplemented + " are defined in the context service, but never implemented in " + componentName);
        }

    	/*
         * Check the context entity id was specified
         */
        if (configuration.get(CONTEXT_ENTITY_ID) == null) {
            throw new ConfigurationException("Try to instantiate a context entity without and context.entity.id element");
        }
        stateIds.add(CONTEXT_ENTITY_ID);
        update(CONTEXT_ENTITY_ID,configuration.get(CONTEXT_ENTITY_ID));

        /*
         * Initialize the state map with the configured values
         */
        for (String configuredState : getConfiguredStates(configuration)) {
            if (stateIds.contains(configuredState)) {
                update(configuredState, getStateConfiguredValue(configuredState, configuration));
            }
            else {
                warn("Configured state " + configuredState + " will be ignored, it is not defined in the context services of " + componentName);
            }
        }
    }


    /**
     * Get the definition of the states associated to a given context service
     */
    private void extractDefinedStatesForService(Class<?> service) {

    	/*
    	 * consider only context services
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
                    stateIds.add(contextServiceName+"."+stateName);
                } catch (IllegalArgumentException | IllegalAccessException ignored) {
                }

            }
        }
        for (Class<?> inheritedService : service.getInterfaces()){
            extractDefinedStatesForService(inheritedService);
        }
    }

    /**
     * This class keeps track of all the information required to schedule periodic tasks
     */
    public class PeriodicTask {

        private final Consumer<InstanceManager> action;

        private final long period;

        private final TimeUnit unit;

        private ManagedScheduledFutureTask<?> taskHandle;

        private PeriodicTask(Consumer<InstanceManager> action, long period, TimeUnit unit) {
            this.action = action;
            this.period	= period;
            this.unit	= unit;

    		/*
    		 * If the instance is active schedule the task immediately
    		 */
            if (isInstanceActive()) {
                start();
            }
        }

        /**
         * Start executing the action periodically
         */
        public void start() {

			/*
			 * Handle non periodic tasks, as a single shot activation
			 */
            if (period < 0 && isInstanceActive()) {
                action.accept(getInstanceManager());
            }
            else if (period > 0) {
                taskHandle =	getScheduler().scheduleAtFixedRate(	() -> {
                    if (isInstanceActive()) {
                        action.accept(getInstanceManager());
                    }
                },period,period,unit);
            }
        }

        /**
         * Stop executing the action
         */
        public void stop() {
            if (taskHandle != null) {
                taskHandle.cancel(true);
                taskHandle = null;
            }
        }
    }

    /**
     * Add a new periodic task that will be executed on behalf of registered interceptors.
     *
     * The action to be performed is specified as a consumer that will be given access to the component
     * instance
     */
    public PeriodicTask schedule(Consumer<InstanceManager> action, long period, TimeUnit unit) {
        PeriodicTask task = new PeriodicTask(action,period,unit);
        return task;
    }

    /**
     * Utility function to handle optional configuration
     */
    protected final static Element[] EMPTY_OPTIONAL = new Element[0];

    protected final static Element[] optional(Element[] elements) {
        return elements != null ? elements : EMPTY_OPTIONAL;
    }

    /**
     * Cast a string value to the type of the specified field
     */
    protected final static Object cast(InstanceManager component, FieldMetadata field, String value) {
        try {
            Class<?> type = Property.computeType(field.getFieldType(),component.getGlobalContext());
            return Property.create(type,value);
        }
        catch (ConfigurationException ignored) {
            return null;
        }
    }

    /**
     * Verify the type of the specified value matches the type of field
     */
    protected final static boolean hasValidType(InstanceManager component, FieldMetadata field, Object value) {
        try {
            Class<?> type = boxed(Property.computeType(field.getFieldType(),component.getGlobalContext()));
            return type.isInstance(value);
        }
        catch (ConfigurationException ignored) {
            return false;
        }
    }

    /**
     * Get a boxed class that can be used to determine if an object reference is instance of the given class,
     * even in the case of primitive types.
     *
     * NOTE notice that Class.isInstance always returns false for primitive types. So we need to use the appropiate
     * wrapper when testing for asignment comptibility.
     */
    protected final static  Class<?> boxed(@SuppressWarnings("rawtypes") Class type) {
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

    /**
     * Get the list of configured states of the instance
     */
    protected final static Set<String> getConfiguredStates(Dictionary<String,?> configuration) {
        @SuppressWarnings("unchecked")
        Map<String,Object> stateConfiguration = (Map<String,Object>) configuration.get("context.entity.init");

        if (stateConfiguration == null)
            return Collections.emptySet();

        return stateConfiguration.keySet();
    }

    /**
     * Get the value of a state defined in the instance configuration
     */
    protected final static Object getStateConfiguredValue(String stateId, Dictionary<String,?> configuration) {

        @SuppressWarnings("unchecked")
        Map<String,Object> stateConfiguration = (Map<String,Object>) configuration.get("context.entity.init");

        if (stateConfiguration == null)
            return null;

        return stateConfiguration.get(stateId);
    }

    /**
     * Set the value of a state  in the instance configuration
     */
    protected final static void setStateConfiguredValue(String stateId, Object value, Dictionary<String,Object> configuration) {

        @SuppressWarnings("unchecked")
        Map<String,Object> stateConfiguration = (Map<String, Object>) configuration.get("context.entity.init");

        if (stateConfiguration == null) {
            stateConfiguration = new HashMap<>();
            configuration.put("context.entity.init",stateConfiguration);
        }

        stateConfiguration.put(stateId,value);
    }
}
