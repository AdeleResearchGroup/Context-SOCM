package fr.liglab.adele.cream.model;

import java.util.Map;
import java.util.Set;

/**
 * Created by aygalinc on 15/09/15.
 */
public interface ContextEntity {

    public static final String CONTEXT_ENTITY_ID = "context.entity.id";

    public Set<String> getServices();

    public Object getValue(String state);

    public default String getId() {
    	return (String) getValue(CONTEXT_ENTITY_ID);
    }

    public Set<String> getStates();

    public Map<String, Object> getValues();
}
