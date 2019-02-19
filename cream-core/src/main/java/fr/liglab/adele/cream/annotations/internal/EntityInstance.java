package fr.liglab.adele.cream.annotations.internal;

import java.lang.reflect.InvocationHandler;

import org.apache.felix.ipojo.ComponentInstance;

public interface EntityInstance extends ComponentInstance {

	/**
	 * Get the invocation handler used to delegate method invocation to the pojo associated to the instance
	 */
	public abstract InvocationHandler getDelegationHandler();

}
