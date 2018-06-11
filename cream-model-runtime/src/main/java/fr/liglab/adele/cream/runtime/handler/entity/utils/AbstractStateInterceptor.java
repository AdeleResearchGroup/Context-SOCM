package fr.liglab.adele.cream.runtime.handler.entity.utils;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by aygalinc on 26/08/16.
 */
public abstract class AbstractStateInterceptor implements StateInterceptor {

    /**
     * The associated entity handler in charge of keeping the context state
     */
    protected final ContextStateHandler stateHandler;

    /**
     * The states handled by this interceptor
     */
    private final Set<String> states = new HashSet<>();

    /**
     * The mapping from fields handled by this interceptor to states of the context
     */
    private final Map<String, String> fieldToState = new HashMap<>();

    protected AbstractStateInterceptor(ContextStateHandler stateHandler) {
        this.stateHandler = stateHandler;
    }

    protected String getId(Element state) {
    	return state.getAttribute("id");
    }

    /**
     * Configures the interceptor from the state metadata
     */
    @Override
    public void configure(Element state, Dictionary<String,Object> configuration) throws ConfigurationException {

        String stateId 		= getId(state);
        String stateField	= state.getAttribute("field");

		/*
         * Check the association field to state
		 */
        if (stateField == null) {
            throw new ConfigurationException("Malformed Manifest : a state variable is declared with no 'field' attribute");
        }

        FieldMetadata fieldMetadata = stateHandler.getPojoMetadata().getField(stateField);
        if (fieldMetadata == null) {
            throw new ConfigurationException("Malformed Manifest : the specified field doesn't exists " + stateField);
        }

        states.add(stateId);
        fieldToState.put(stateField,stateId);
        
        stateHandler.getInstanceManager().register(fieldMetadata, this);
    }
    

    @Override
    public void reconfigure(Dictionary<String,Object> configuration) {
    }

    @Override
    public Object onGet(Object pojo, String fieldName, Object value) {
        return stateHandler.getProperty(getStateForField(fieldName));
    }

    @Override
    public void onSet(Object pojo, String fieldName, Object value) {
    	stateHandler.update(getStateForField(fieldName),value);
    }

    @Override
    public void validate() {
    }

    @Override
    public void invalidate() {
    }

    protected boolean isConfigured(String state) {
    	return states.contains(state);
    }

    protected String getStateForField(String field) {
    	return fieldToState.get(field);
    }

}
