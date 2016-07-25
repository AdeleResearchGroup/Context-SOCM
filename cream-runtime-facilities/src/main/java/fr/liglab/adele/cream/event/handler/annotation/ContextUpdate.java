package fr.liglab.adele.cream.event.handler.annotation;

/**
 * Created by aygalinc on 09/06/16.
 */
public @interface ContextUpdate {

    Class specification();

    String stateId();

    String STATE_ID_ATTRIBUTE = "stateId";

    String SPECIFICATION_ATTRIBUTE = "specification";
}
