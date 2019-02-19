package fr.liglab.adele.cream.runtime.internal.proxies.dispatcher;

import java.lang.reflect.Method;


/**
 * 
 * 
 * <p>A method dispatcher is an object that allows to efficiently invoke a method on an object without reflection.
 * 
 * <p>Method dispatchers use a jump table automatically generated from an interface definition {@link MethodDispatcherGenerator} that
 * allows to use a method identifier to perform the invocation. 
 * 
 * <p>Created by aygalinc on 09/09/16.
 * 
 */
public interface MethodDispatcher {

	/**
	 * The returned object if the requested method is not implemented in the interface
	 */
	public static String UNKNOWN_METHOD = "MethodDispatcher$NoSuchMethod";
	
	/**
	 * Verifies if the invocation result was returned by dispatching to an invalid method
	 */
	public static Object verify(Object result) throws NoSuchMethodException {
	
		if (result != null && result instanceof String && UNKNOWN_METHOD.equals(result)) {
			throw new NoSuchMethodException();
		}
		
		return result;
	}
	
    /**
     * Dispatches a method invocation to a specified pojo 
     */
    public default Object dispatch(Object pojo, Method method, Object[] args) throws Throwable {
    	return dispatch(pojo,id(method),args);
    }

    /**
     * Efficiently dispatches a method invocation using a method identifier and a jump table
     * 
     * @generated
     */
    public Object dispatch(Object pojo, int methodId, Object[] args) throws Throwable;


    /**
     * The unique identifier of the method in the interface 
     *
     * <p>TODO IMPORTANT we should verify that {@link java.lang.reflect.Method#hashCode} can be effectively used as
     * identifier  
	 *
     */
    public static int id(Method method) {
    	return method.hashCode();
    }
}
