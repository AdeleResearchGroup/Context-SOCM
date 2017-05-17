package fr.liglab.adele.cream.runtime.handler.entity.utils;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aygalinc on 26/08/16.
 */
public abstract class AbstractStateInterceptor implements StateInterceptor {

    /**
     * The mapping from fields handled by this interceptor to states of the context
     */
    protected final Map<String, String> fieldToState = new HashMap<>();

    /**
     * The mapping from fields handled by this interceptor to states of the context
     */
    protected final Map<String, String> stateToField = new HashMap<>();

    /**
     * Adds a new managed field
     */
    public void handleState(InstanceManager component, PojoMetadata componentMetadata, Element state) throws ConfigurationException {

        String stateId = state.getAttribute("id");
        String stateField = state.getAttribute("field");

		/*
         * Check the association field to state
		 */
        if (stateField == null) {
            throw new ConfigurationException("Malformed Manifest : a state variable is declared with no 'field' attribute");
        }

        FieldMetadata fieldMetadata = componentMetadata.getField(stateField);
        if (fieldMetadata == null) {
            throw new ConfigurationException("Malformed Manifest : the specified field doesn't exists " + stateField);
        }

        fieldToState.put(stateField, stateId);
        stateToField.put(stateId,stateField);
        component.register(fieldMetadata, this);
    }
}
