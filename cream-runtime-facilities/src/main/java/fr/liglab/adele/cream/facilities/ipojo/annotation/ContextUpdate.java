package fr.liglab.adele.cream.facilities.ipojo.annotation;

/**
 * Created by aygalinc on 09/06/16.
 */
public @interface ContextUpdate {

    String STATE_ID_ATTRIBUTE = "stateId";

    String SPECIFICATION_ATTRIBUTE = "specification";

    Class specification();

    String stateId();

}
