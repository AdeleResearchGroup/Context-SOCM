package fr.liglab.adele.cream.facilities.ipojo.annotation;

/**
 * Created by aygalinc on 09/06/16.
 */
public interface FacilitiesHandlerReference {


    /**
     * Event Handler Namespace
     */
    public static final String FACILITIES_HANDLER_NAMESPACE = "fr.liglab.adele.cream.facilities.ipojo.annotation";

    /**
     * The handler in charge of managing event call
     */
    public static final String EVENT_HANDLER_NAME = "ContextUpdate";

    /**
     * The handler in charge of check context service
     */
    public static final String CONTEXT_REQUIREMENT_HANDLER_NAME = "ContextRequirement";

    public static final String CONTEXT_REQUIREMENT_SPEC_ATTRIBUTE_NAME = "spec";
}
