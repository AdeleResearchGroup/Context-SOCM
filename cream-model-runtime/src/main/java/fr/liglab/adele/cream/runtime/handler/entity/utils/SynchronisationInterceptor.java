package fr.liglab.adele.cream.runtime.handler.entity.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Member;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.FieldInterceptor;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.MethodInterceptor;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.MethodMetadata;

import fr.liglab.adele.cream.annotations.internal.ReservedCreamValueReference;


/**
 * Interceptor to handle state fields that are not handler by direct access, but using synchronization
 * functions (push,pull,apply)
 */
public class SynchronisationInterceptor extends AbstractStateInterceptor implements StateInterceptor, FieldInterceptor, MethodInterceptor {

    /**
     * The accessor/mutator functions associated to a state
     */
    private final Map<String, BiConsumer<Object, Object>> applyFunctions	= new HashMap<>();
    private final Map<String, Function<Object, Object>> pullFunctions 		= new HashMap<>();

    /**
     * The periodic task associated to a given state 
     */
    private final Map<String, ContextStateHandler.PeriodicTask> pullTasks = new HashMap<>();

    /**
     * The mapping from push methods handled by this interceptor to states of the context
     */
    private final Map<String, String> methodToState = new HashMap<>();

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(SynchronisationInterceptor.class);


    /**
     * @param abstractContextHandler
     */
    public SynchronisationInterceptor(ContextStateHandler stateHandler) {
        super(stateHandler);
    }

    @Override
    public void configure(Element state, Dictionary<String,Object> configuration) throws ConfigurationException {

        super.configure(state,configuration);
        
        String stateId 		= getId(state);
        
        /*
         * If a pull function was defined, register a function that will be invoked on every field access 
         */
        String pull 	= state.getAttribute("pull");

        if (pull != null) {

	    	/*
             * Verify the type of the pull field is a Supplier
	    	 * 
	    	 * TODO iPOJO metadata doesn't handle generic types. We could use reflection on the component class to validate
	    	 * that the pull field is a Supplier of the type of the state field
	    	 */
            FieldMetadata pullFieldMetadata = stateHandler.getPojoMetadata().getField(pull);
            String pullFieldType = FieldMetadata.getReflectionType(pullFieldMetadata.getFieldType());
            if (!pullFieldType.equals(Supplier.class.getCanonicalName())) {
                throw new ConfigurationException("Malformed Manifest : the specified pull field " + pull + " must be of type " + Supplier.class.getName());
            }

            Long period 	= Long.valueOf(state.getAttribute("period"));
            TimeUnit unit 	= TimeUnit.valueOf(state.getAttribute("unit"));

	    	/*
	    	 * The field access handler. 
	    	 * 
	    	 * Notice that the lambda expression captures the value of some variables from configuration time to actual
	    	 * access time. 
	    	 */
            
            pullFunctions.put(stateId, (Object pojo) -> {
                
            	@SuppressWarnings("unchecked")
				Supplier<Object> supplier = (Supplier<Object>) stateHandler.getInstanceManager().getFieldValue(pull,pojo);
                
                return supplier.get();
            });
            
            pullTasks.put(stateId, stateHandler.schedule(stateId, this::periodic, period, unit));

        }

        /*
         * If an apply function was defined, register a function that will be invoked on every field update 
         */
        String apply = state.getAttribute("apply");
        if (apply != null) {

	    	/*
	    	 * Verify the type of the apply field is a Consumer 
	    	 * 
	    	 * TODO iPOJO metadata doesn't handle generic types. We could use reflection on the component class to validate
	    	 * that the apply field is a Consumer of the type of the state field
	    	 */
            FieldMetadata applyFieldMetadata = stateHandler.getPojoMetadata().getField(apply);
            String applyFieldType = FieldMetadata.getReflectionType(applyFieldMetadata.getFieldType());
            if (!applyFieldType.equals(Consumer.class.getCanonicalName())) {
                throw new ConfigurationException("Malformed Manifest : the specified apply field " + apply + " must be of type " + Consumer.class.getName());
            }
	    	
	    	/*
	    	 * The field access handler. 
	    	 * 
	    	 * Notice that the lambda expression captures the value of some variables from configuration time to actual
	    	 * access time. 
	    	 */
            applyFunctions.put(stateId, (Object pojo, Object value) -> {
            	
                @SuppressWarnings("unchecked")
				Consumer<Object> consumer = (Consumer<Object>) stateHandler.getInstanceManager().getFieldValue(apply, pojo);
                
                consumer.accept(value);
            });
        }

        String push = state.getAttribute("push");
        if (push != null) {
	    	
	    	/*
	    	 * Verify the push method is correctly defined
	    	 * 
	    	 * TODO we should verify the return type if the method matches the type of the state field
	    	 */
            MethodMetadata stateMethod = stateHandler.getPojoMetadata().getMethod(push);
            if (stateMethod == null) {
                throw new ConfigurationException("Malformed Manifest : the specified method doesn't exists " + stateMethod);
            }

            methodToState.put(push, stateId);
            stateHandler.getInstanceManager().register(stateMethod,this);
        }
    }

    @Override
    public void reconfigure(Dictionary<String,Object> configuration) {
    	     	
    	if (configuration.get(ReservedCreamValueReference.RECONFIGURATION_FREQUENCY.toString()) == null) {
            return;
        }

        @SuppressWarnings("unchecked")
		Map<String,Map<String, Object>> reconfiguration = (Map<String,Map<String, Object>>) configuration.get(ReservedCreamValueReference.RECONFIGURATION_FREQUENCY.toString());

        for (Map.Entry<String,Map<String, Object>> paramToReconfigure : reconfiguration.entrySet()) {
        	
            String stateId = paramToReconfigure.getKey();
            
            if (! isConfigured(stateId)) {
            	continue;
            }
            
            ContextStateHandler.PeriodicTask pullTask = pullTasks.get(stateId);
            if (pullTask == null) {
                LOG.warn("Cannot reconfigure state :" + paramToReconfigure.getKey() + " cause : no pull function available");
                continue;
            }

            	
        	Map<String, Object> reconfigurationParameters = paramToReconfigure.getValue();
        	
            Long period = (Long) reconfigurationParameters.get(ReservedCreamValueReference.RECONFIGURATION_FREQUENCY_PERIOD.toString());
            TimeUnit unit = (TimeUnit) reconfigurationParameters.get(ReservedCreamValueReference.RECONFIGURATION_FREQUENCY_UNIT.toString());

        	pullTask.reconfigure(period,unit);
        }
    }

    /**
     * The kind of notification
     *
     */
    public enum NotificationKind implements StateInterceptor.Context {
    	PUSH,
    	PULL,
    	PERIODIC;
    } 

    /**
     * This class notifies the state handler and its listeners of an update of a field (due to a pull/push access)
     * 
     * It keeps a history of notifications to avoid reentrant pulling, as it may lead to an infinite cascade of notifications if the
     * listener of the notification request the value of the state again and it changes at every invocation (as is the case for 
     * continuous temporal measurements). 
     *  
     */
    private class Notifier extends ThreadLocal<Set<String>> {
    	
    	public void update(String state, Supplier<Object> value, NotificationKind kind) {

        	if (inProgress(state)) {
        		return;
        	}

        	try {
        		startNotification(state);
   	            stateHandler.update(Optional.ofNullable(kind),state,value.get());
        	}
        	finally {
        		endNotification(state);
        	}

    	}

    	private boolean inProgress(String state) {
    		Set<String> notifying = get(); 
    		return notifying != null && notifying.contains(state);
    	}

    	private void startNotification(String state) {
    		
    		Set<String> notifying = get();
    		
    		if (notifying == null) {
    			notifying = new HashSet<>();
    			set(notifying);
    		}
    		
    		notifying.add(state);
    	}
    	
    	private void endNotification(String state) {
    		
    		Set<String> notifying = get();
    		notifying.remove(state);
    		
    		if (notifying.isEmpty()) {
    			remove();
    		}
    	}
    }

    private Notifier notifier = this.new Notifier();

     
    /**
     * Pulls a new value using the specified function, and update the cached state.
     * 
     */
    private void pull(Object pojo, String state) {
 		Function<Object, Object> pullFunction = pullFunctions.get(state);
        if (pullFunction != null) {
            notifier.update(state, () -> pullFunction.apply(pojo), NotificationKind.PULL);
        }
    }

    private void periodic(InstanceManager instance, String state) {
 		Function<Object, Object> pullFunction = pullFunctions.get(state);
        if (pullFunction != null) {
            notifier.update(state, () -> pullFunction.apply(instance.getPojoObject()), NotificationKind.PERIODIC);
        }
    }

    private void apply(Object pojo, String state, Object value) {

    	BiConsumer<Object, Object> applyFunction = applyFunctions.get(state);
        if (applyFunction != null && pojo != null && value != null) {
            applyFunction.accept(pojo, value);
        }
    }

    private void push(Object pojo, String state, Object value) {
   		notifier.update(state, () -> value, NotificationKind.PUSH);
    }
    
    @Override
    public Object onGet(Object pojo, String fieldName, Object value) {
    	pull(pojo, getStateForField(fieldName));
        return super.onGet(pojo, fieldName, value);
    }



    @Override
    public void onSet(Object pojo, String fieldName, Object value) {
    	apply(pojo, getStateForField(fieldName),value);
    }
    

    @Override
    public void onExit(Object pojo, Member method, Object value) {
    	push(pojo,methodToState.get(method.getName()),value);
    }


    @Override
    public void getInterceptorInfo(String stateId, Element stateDescription) {
    	
    	if (! isConfigured(stateId)) {
    		
    		stateDescription.addAttribute(new Attribute("applyFunction","false"));
    		stateDescription.addAttribute(new Attribute("pullFunction","false"));
    		stateDescription.addAttribute(new Attribute("pushFunction","false"));
            
    		return;
    	}
    	
    	stateDescription.addAttribute(new Attribute("applyFunction",Boolean.toString(applyFunctions.containsKey(stateId))));
    	stateDescription.addAttribute(new Attribute("pushFunction",Boolean.toString(methodToState.containsValue(stateId))));
    	
    	stateDescription.addAttribute(new Attribute("pullFunction",Boolean.toString(pullFunctions.containsKey(stateId))));
    	ContextStateHandler.PeriodicTask task = pullTasks.get(stateId);
    	if (task != null) {
    		stateDescription.addAttribute(new Attribute("period", String.valueOf(task.getPeriod())));
    		stateDescription.addAttribute(new Attribute("unit", task.getUnit().toString()));
    	}
    }

    @Override
    public void validate() {
        for (ContextStateHandler.PeriodicTask pullTask : pullTasks.values()) {
            pullTask.start();
        }
    }

    @Override
    public void invalidate() {
        for (ContextStateHandler.PeriodicTask pullTask : pullTasks.values()) {
            pullTask.stop();
        }
    }


    @Override
    public void onEntry(Object pojo, Member method, Object[] args) {
    }

    @Override
    public void onError(Object pojo, Member method, Throwable throwable) {
    }

    @Override
    public void onFinally(Object pojo, Member method) {
    }

}