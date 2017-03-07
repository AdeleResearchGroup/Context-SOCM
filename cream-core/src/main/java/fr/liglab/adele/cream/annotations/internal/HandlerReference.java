package fr.liglab.adele.cream.annotations.internal;

/**
 * This class keeps the shared identifiers that allow to map a given annotation to a runtime handler
 * that handles it
 *
 * @author vega
 */
public interface HandlerReference {

    /**
     * The namespace associated to context handlers
     */
    public static final String NAMESPACE = "fr.liglab.adele.cream.runtime.handler";

    /**
     * The handler in charge of managing entities
     */
    public static final String ENTITY_HANDLER = "entity";

    /**
     * The handler in charge of managing entities
     */
    public static final String FUNCTIONAL_EXTENSION_ENTITY_HANDLER = "functional-extension-entity";

    /**
     * The handler in charge of managing relations
     */
    public static final String RELATION_HANDLER = "relation";

    /**
     * The handler in charge of managing creators
     */
    public static final String CREATOR_HANDLER = "creation";

    /**
     * The handler in charge of the external control of behavior component
     */
    String FUNCTIONAL_EXTENSION_LIFECYCLE_HANDLER = "functional-extension-lifecycle";

    /**
     * The handler in charge of behavior tracking, instantiation and property propagation
     */
    String FUNCTIONAL_EXTENSION_TRACKER_HANDLER = "functional-extension-tracker";

}
