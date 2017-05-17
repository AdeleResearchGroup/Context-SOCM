package fr.liglab.adele.cream.runtime.handler.entity.utils;


import org.apache.felix.ipojo.FieldInterceptor;
import org.apache.felix.ipojo.metadata.Element;

/**
 * This is the base class for all interceptors that are charged to handle the instrumenttaion
 * of context state fields.
 *
 * @author vega
 */
public interface StateInterceptor extends FieldInterceptor {

    /**
     * Notifies the interceptor that the iPOJO instance has been activated
     */
    void validate();

    /**
     * Notifies the interceptor that the iPOJO instance has been invalidated.
     */
    void invalidate();

    void addInterceptorDescription(Element stateElement);
}
