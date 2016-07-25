package fr.liglab.adele.cream.runtime.handler.entity.utils;


/**
 * This is the base class for all interceptors that are charged to handle the instrumenttaion
 * of context state fields.
 * 
 * @author vega
 *
 */
public interface StateInterceptor {

	/**
	 * Notifies the interceptor that the iPOJO instance has been activated
	 */
	void validate();
	
	/**
	 * Notifies the interceptor that the iPOJO instance has been invalidated.
	 */
	void invalidate();
}
