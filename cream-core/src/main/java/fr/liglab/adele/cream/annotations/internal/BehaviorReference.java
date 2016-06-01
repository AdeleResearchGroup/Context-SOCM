package fr.liglab.adele.cream.annotations.internal;

/**
 * Created by aygalinc on 01/06/16.
 */
public interface BehaviorReference {

    /**
     * The namespace associated to context handlers
     */
    public static final String NAMESPACE = "fr.liglab.adele.cream.runtime.behavior";

    /**
     * The handler in charge of managing entities
     */
    public static final String DEFAULT_BEHAVIOR_NAME = "default";

    public static final String SPEC_ATTR_NAME = "specification";

    public static final String IMPLEM_ATTR_NAME = "implementation";

    public static final String BEHAVIOR_TYPE_PROPERTY = "behavior.factory.property";

    public static final String BEHAVIOR_TYPE = "behavior.factory" ;
}
