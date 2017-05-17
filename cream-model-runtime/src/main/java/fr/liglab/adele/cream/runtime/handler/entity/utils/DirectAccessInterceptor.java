package fr.liglab.adele.cream.runtime.handler.entity.utils;

import org.apache.felix.ipojo.FieldInterceptor;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import java.util.Map;

/**
 * Interceptor to handle state fields that are directly manipulated by the entity code
 */
public class DirectAccessInterceptor extends AbstractStateInterceptor implements StateInterceptor, FieldInterceptor {

    /**
     * The associated entity handler in charge of keeping the context state
     */
    private final AbstractContextHandler abstractContextHandler;

    /**
     * @param abstractContextHandler
     */
    public DirectAccessInterceptor(AbstractContextHandler abstractContextHandler) {
        this.abstractContextHandler = abstractContextHandler;
    }

    @Override
    public Object onGet(Object pojo, String fieldName, Object value) {
        return abstractContextHandler.getStateValue(fieldToState.get(fieldName));
    }

    @Override
    public void onSet(Object pojo, String fieldName, Object value) {
        abstractContextHandler.update(fieldToState.get(fieldName), value);
    }

    @Override
    public void validate() {
        //Do nothing
    }

    @Override
    public void invalidate() {
        //Do nothing
    }

    @Override
    public void addInterceptorDescription(Element stateElement) {
        String stateId = stateElement.getAttribute("id");
        if (fieldToState.values().contains(stateId)){
            stateElement.addAttribute(new Attribute("directAcess","true"));
        }else {
            stateElement.addAttribute(new Attribute("directAcess","false"));
        }
    }

    @Override
    public void handleReconfiguration(Map<String,Object> dictionary) {
        //DO NOTHING, this interceptor cannot handle a reconfiguration
    }


}