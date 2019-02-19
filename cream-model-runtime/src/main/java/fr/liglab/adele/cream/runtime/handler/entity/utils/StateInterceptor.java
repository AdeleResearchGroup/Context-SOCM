package fr.liglab.adele.cream.runtime.handler.entity.utils;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.ContextListener;
import org.apache.felix.ipojo.ContextSource;
import org.apache.felix.ipojo.FieldInterceptor;
import org.apache.felix.ipojo.metadata.Element;

import java.util.Dictionary;
import java.util.Optional;

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

    /**
     * This class represents some additional information to pass from the interceptor to specialized context listeners
     */
    public interface Context {
    	
    }
    
    /**
     * This class represent an specialized context listener that can handle additional information from the interceptor
     */
    public interface Listener extends ContextListener {
    	
    	@Override
    	public default void update(ContextSource source, String property, Object value) {
    		update(source,Optional.empty(),property,value);
    	}

        /**
         * A monitored value has been modified by the specified state interceptor
         */
		public void update(ContextSource source, Optional<? extends StateInterceptor.Context> extra, String property, Object value);

    }
}
