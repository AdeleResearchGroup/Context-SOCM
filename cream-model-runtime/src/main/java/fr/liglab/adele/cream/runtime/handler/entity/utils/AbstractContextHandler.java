package fr.liglab.adele.cream.runtime.handler.entity.utils;

import fr.liglab.adele.cream.model.ContextEntity;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.util.Property;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledFutureTask;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by aygalinc on 19/07/16.
 */
public abstract class AbstractContextHandler extends PrimitiveHandler implements ContextEntity {

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
