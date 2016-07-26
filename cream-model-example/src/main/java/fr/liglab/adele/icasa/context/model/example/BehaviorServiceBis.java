package fr.liglab.adele.icasa.context.model.example;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

/**
 * Created by aygalinc on 01/06/16.
 */
public @ContextService interface BehaviorServiceBis {

    public static final @State String BEHAVIOR_STATE = "behaviorparam";

    public String coucouBis();
}
