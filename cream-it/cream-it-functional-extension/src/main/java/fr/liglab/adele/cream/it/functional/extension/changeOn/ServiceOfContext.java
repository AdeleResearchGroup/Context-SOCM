package fr.liglab.adele.cream.it.functional.extension.changeOn;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

/**
 * Created by aygalinc on 15/09/16.
 */
public @ContextService interface ServiceOfContext {

    @State String STATE_1 = "state_1";

    public void setState1(boolean newState);

}
