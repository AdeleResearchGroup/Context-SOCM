package fr.liglab.adele.cream.runtime.handler.entity.behavior;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.runtime.handler.entity.utils.AbstractContextHandler;
import fr.liglab.adele.cream.runtime.handler.entity.utils.DirectAccessInterceptor;
import fr.liglab.adele.cream.runtime.handler.entity.utils.StateInterceptor;
import fr.liglab.adele.cream.runtime.handler.entity.utils.SynchronisationInterceptor;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Handler(name = HandlerReference.BEHAVIOR_ENTITY_HANDLER ,namespace = HandlerReference.NAMESPACE)
public class BehaviorEntityHandler extends AbstractContextHandler {

    /**
     * The list of exposed context services
     */
    private final Set<String> services 					= new HashSet<>();

    /**
     * The list of states defined in the implemented context services
     */
    private final Set<String> stateIds 					= new HashSet<>();

    /**
     * The list of interceptors in charge of handling each state field
     */
    private final Set<StateInterceptor>	interceptors	= new HashSet<>();

    /**
     * The current values of the state properties
     */
    private final Map<String,Object> stateValues 		= new ConcurrentHashMap<>();

    private boolean instanceIsActive=false;

    @Override
    protected boolean isInstanceActive() {
        return instanceIsActive;
    }

    /**
     * The Wisdom Scheduler used to handle periodic tasks
     */
    @Requires(id="scheduler",proxy = false)
    public ManagedScheduledExecutorService scheduler;


    @Override
    protected ManagedScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     *
     * Handler Lifecycle
     */
    @Override
    public synchronized void stateChanged(int state) {

        if (state == InstanceManager.VALID) {
            instanceIsActive = true;

            /** TODO
             propagate(new Hashtable<>(stateValues));
             **/
            /*
             * restart state handlers
             */
            for (StateInterceptor interceptor : interceptors) {
                interceptor.validate();
            }
        }

        if (state == InstanceManager.INVALID) {
            instanceIsActive = false;

            /*
             * stop state handlers
             */
            for (StateInterceptor interceptor : interceptors) {
                interceptor.invalidate();
            }

        }
    }


    @Override
    public synchronized void start() {

    }

    @Override
    public synchronized void stop() {

    }

    /**
     * Handler Configuration
     *
     **/
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void configure(Element element, Dictionary rawConfiguration) throws ConfigurationException {


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
        for (Element entity : optional(element.getElements(HandlerReference.BEHAVIOR_ENTITY_HANDLER,HandlerReference.NAMESPACE))) {
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
     * Handler Propagation
     */

    /**
     * Updates the value of a state property, propagating the change to the published service properties
     * @param stateId
     * @param value
     */
    @Override
    public void update(String stateId, Object value) {

        assert stateId != null && stateIds.contains(stateId);

        Object oldValue 	= stateValues.get(stateId);
        boolean noChange 	= (oldValue == null && value == null) || (oldValue != null && value != null) && oldValue.equals(value);

        if (noChange)
            return;

        if (value != null) {
            stateValues.put(stateId, value);
        }
        else {
            stateValues.remove(stateId);
        }
        //TODO:PRopagate the value change !
    }

    /**
     *
     * Context Entity Implementation
     *
     */

    @Override
    public Set<String> getServices() {
        return services;
    }

    @Override
    public String getId() {
        return (String) stateValues.get(CONTEXT_ENTITY_ID);
    }

    @Override
    public Object getStateValue(String state) {
        if (state == null)
            return null;

        return stateValues.get(state);
    }

    @Override
    public Set<String> getStates() {
        return new HashSet<>(stateIds);
    }

    @Override
    public Map<String, Object> dumpState() {
        return new HashMap<>(stateValues);
    }

    @Override
    public HandlerDescription getDescription() {
        return new BehaviorEntityHandlerDescription();
    }

    /**
     * The description of the handler.
     *
     * This class exposes the generic interface ContextEntity to allow external code to introspect the
     * component instance and obtain the current state values.
     *
     */
    public class BehaviorEntityHandlerDescription extends HandlerDescription implements ContextEntity {

        private BehaviorEntityHandlerDescription() {
            super(BehaviorEntityHandler.this);
        }

        @Override
        public Set<String> getServices() {
            return BehaviorEntityHandler.this.getServices();
        }

        @Override
        public String getId() {
            return BehaviorEntityHandler.this.getId();
        }

        @Override
        public Object getStateValue(String getStateValue) {
            return BehaviorEntityHandler.this.getStateValue(getStateValue);
        }

        @Override
        public Set<String> getStates() {
            return BehaviorEntityHandler.this.getStates();
        }

        @Override
        public Map<String, Object> dumpState() {
            return BehaviorEntityHandler.this.dumpState();
        }

        @Override
        public Element getHandlerInfo() {
            Element handlerInfo = super.getHandlerInfo();

            for (Map.Entry<String,Object> entry:dumpState().entrySet()){
                Element stateElement = new Element("state",null);
                stateElement.addAttribute(new Attribute(entry.getKey(),entry.getValue().toString()));
                handlerInfo.addElement(stateElement);
            }

            return handlerInfo;
        }
    }
}
