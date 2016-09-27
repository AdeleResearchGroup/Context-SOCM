package fr.liglab.adele.cream.it.behavior.contextSource;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

/**
 * Created by aygalinc on 26/09/16.
 */
public @ContextService interface BehaviorService1 {

    @State String BEHAVIOR_STATE="state";

    void setValue(String filterValue);
}
