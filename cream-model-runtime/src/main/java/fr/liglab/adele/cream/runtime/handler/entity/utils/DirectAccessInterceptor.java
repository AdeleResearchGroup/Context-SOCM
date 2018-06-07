package fr.liglab.adele.cream.runtime.handler.entity.utils;

import org.apache.felix.ipojo.FieldInterceptor;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

/**
 * Interceptor to handle state fields that are directly manipulated by the entity code, the value is directly stored in the
 * associated state handler
 */
public class DirectAccessInterceptor extends AbstractStateInterceptor implements StateInterceptor, FieldInterceptor {

    /**
     * @param abstractContextHandler
     */
    public DirectAccessInterceptor(ContextStateHandler stateHandler) {
       super(stateHandler);
    }


    @Override
    public void getInterceptorInfo(String stateId, Element stateDescription) {
    	stateDescription.addAttribute(new Attribute("directAcess", Boolean.toString(isConfigured(stateId))));
    }

}