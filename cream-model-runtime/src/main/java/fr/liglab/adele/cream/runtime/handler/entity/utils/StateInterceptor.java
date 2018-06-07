package fr.liglab.adele.cream.runtime.handler.entity.utils;


import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.FieldInterceptor;
import org.apache.felix.ipojo.metadata.Element;

import java.util.Dictionary;

/**
 * This is the base class for all interceptors that are charged to handle the instrumentation
 * of context state fields.
 *
 * @author vega
 */
public interface StateInterceptor extends FieldInterceptor {

	/**
	 * Configures the interceptor from the iPOJO metadata
	 */
    public void configure(Element state, Dictionary<String,Object> configuration) throws ConfigurationException;
    
    /**
     * Notifies the interceptor that the iPOJO instance has been activated
     */
    public void validate();

    /**
     * Notifies the interceptor that the iPOJO instance has been invalidated.
     */
    public void invalidate();

    /**
     * Notifies the interceptor of reconfiguration of the iPOJO instance  
     */
    public void reconfigure(Dictionary<String,Object> configuration);

    /**
     * Adds information regarding this interceptor to the description of the state
     */
    public void getInterceptorInfo(String stateId, Element stateDescription);

}
