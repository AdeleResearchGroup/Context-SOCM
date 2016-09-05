package fr.liglab.adele.cream.runtime.handler.entity.utils;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.FieldInterceptor;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.MethodInterceptor;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Interceptor to handle state fields that are not handler by direct access, but using synchronization
 * functions (push,pull,apply) 
 */
public class SynchronisationInterceptor extends AbstractStateInterceptor implements StateInterceptor, FieldInterceptor, MethodInterceptor {

	/**
	 * The invocation handlers used in every field access
	 */
	private final Map<String,BiConsumer<Object,Object>> applyFunctions	= new HashMap<>();
	private final Map<String,Function<Object,Object>> pullFunctions 	= new HashMap<>();

	/**
	 * The periodic tasks associated to pull fields
	 */
	private final Map<String,AbstractContextHandler.PeriodicTask> pullTasks 	= new HashMap<>();

	/**
	 * The associated entity handler in charge of keeping the context state
	 */
	private final AbstractContextHandler abstractContextHandler;

	/**
	 * The mapping from methods handled by this interceptor to states of the context
	 */
	private final Map<String,String> methodToState = new HashMap<>();

	/**
	 * @param abstractContextHandler
	 */
	public SynchronisationInterceptor(AbstractContextHandler abstractContextHandler) {
		this.abstractContextHandler = abstractContextHandler;
	}

	@Override
	public Object onGet(Object pojo, String fieldName, Object value) {
		Function<Object,Object> pullFunction = pullFunctions.get(fieldName);
		if (pullFunction != null) {
			Object pulledValue =  pullFunction.apply(pojo);
			abstractContextHandler.update(fieldToState.get(fieldName),pulledValue);
		}

		return abstractContextHandler.getStateValue(fieldToState.get(fieldName));
	}

	@Override
	public void onSet(Object pojo, String fieldName, Object value) {
		BiConsumer<Object,Object> applyFunction = applyFunctions.get(fieldName);
		if (applyFunction != null && pojo != null && value != null) {
			applyFunction.accept(pojo,value);
		}
	}

	@Override
	public void onExit(Object pojo, Member method, Object returnedValue) {
		if (returnedValue != null){
			this.abstractContextHandler.update(methodToState.get(method.getName()),returnedValue);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleState(InstanceManager component, PojoMetadata componentMetadata, Element state) throws ConfigurationException {

		super.handleState( component, componentMetadata,state);
		String stateId				= state.getAttribute("id");
		String stateField			= state.getAttribute("field");
        /*
         * If a pull function was defined, register a function that will be invoked on every field access 
         */
		String pull = state.getAttribute("pull");
		if (pull != null) {

	    	/*
	    	 * Verify the type of the pull field is a Supplier 
	    	 * 
	    	 * TODO iPOJO metadata doesn't handle generic types. We could use reflection on the component class to validate
	    	 * that the pull field is a Supplier of the type of the state field
	    	 */
			FieldMetadata pullFieldMetadata = componentMetadata.getField(pull);
			String pullFieldType			= FieldMetadata.getReflectionType(pullFieldMetadata.getFieldType());
			if (! pullFieldType.equals(Supplier.class.getCanonicalName())) {
				throw new ConfigurationException("Malformed Manifest : the specified pull field "+pull+" must be of type "+Supplier.class.getName());
			}
	    	
	    	/*
	    	 * The field access handler. 
	    	 * 
	    	 * Notice that the lambda expression used capture the value of some variables from configuration time to actual
	    	 * access time. 
	    	 */
			pullFunctions.put(stateField, (Object pojo) -> {
				Supplier<Object> supplier = (Supplier<Object>) component.getFieldValue(pull,pojo);
				return supplier.get();
			});
	    	
	    	
	    	/*
	    	 * Register a task associated with the pull function to periodically update the state
	    	 */
			Long period 			= Long.valueOf(state.getAttribute("period"));
			TimeUnit unit			= TimeUnit.valueOf(state.getAttribute("unit"));

			AbstractContextHandler.PeriodicTask pullTask 	= abstractContextHandler.schedule( (InstanceManager instance) -> {
				Function<Object,Object> pullFunction = pullFunctions.get(stateField);
				if (pullFunction != null) {
					Object pulledValue = pullFunction.apply(instance.getPojoObject());
					abstractContextHandler.update(stateId,pulledValue);
				}
			},period,unit);

			pullTasks.put(stateField,pullTask);
		}

        /*
         * If an apply function was defined, register a function that will be invoked on every field access 
         */
		String apply = state.getAttribute("apply");
		if (apply != null) {

	    	/*
	    	 * Verify the type of the apply field is a Consumer 
	    	 * 
	    	 * TODO iPOJO metadata doesn't handle generic types. We could use reflection on the component class to validate
	    	 * that the apply field is a Consumer of the type of the state field
	    	 */
			FieldMetadata applyFieldMetadata = componentMetadata.getField(apply);
			String applyFieldType			= FieldMetadata.getReflectionType(applyFieldMetadata.getFieldType());
			if (! applyFieldType.equals(Consumer.class.getCanonicalName())) {
				throw new ConfigurationException("Malformed Manifest : the specified apply field "+apply+" must be of type "+Consumer.class.getName());
			}
	    	
	    	/*
	    	 * The field access handler. 
	    	 * 
	    	 * Notice that the lambda expression used capture the value of some variables from configuration time to actual
	    	 * access time. 
	    	 */
			applyFunctions.put(stateField, (Object pojo, Object value) -> {
				Consumer<Object> supplier = (Consumer<Object>) component.getFieldValue(apply,pojo);
				supplier.accept(value);
			});
		}

		String push = state.getAttribute("push");
		if (push != null) {
	    	
	    	/*
	    	 * Verify the push method is correctly defined
	    	 * 
	    	 * TODO we should verify the return type if the method matches the type of the state field
	    	 */
			MethodMetadata stateMethod = componentMetadata.getMethod(push);
			if (stateMethod == null) {
				throw new ConfigurationException("Malformed Manifest : the specified method doesn't exists "+stateMethod);
			}

			methodToState.put(push,stateId);
			component.register(stateMethod,this);
		}
	}

	@Override
	public void validate() {
		for (AbstractContextHandler.PeriodicTask pullTask : pullTasks.values()) {
			pullTask.start();
		}
	}

	@Override
	public void invalidate() {
		for (AbstractContextHandler.PeriodicTask pullTask : pullTasks.values()) {
			pullTask.stop();
		}
	}

	@Override
	public void onEntry(Object pojo, Member method, Object[] args) {
		//Do nothing
	}

	@Override
	public void onError(Object pojo, Member method, Throwable throwable) {
		//Do nothing
	}

	@Override
	public void onFinally(Object pojo, Member method) {
		//Do nothing
	}


}