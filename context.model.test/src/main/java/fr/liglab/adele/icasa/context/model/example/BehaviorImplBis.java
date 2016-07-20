package fr.liglab.adele.icasa.context.model.example;


import fr.liglab.adele.cream.annotations.behavior.BehaviorProvider;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

import java.util.function.Supplier;

@BehaviorProvider(spec = BehaviorServiceBis.class)
public class BehaviorImplBis implements BehaviorServiceBis {

    @ContextEntity.State.Field(service = BehaviorServiceBis.class, state = BehaviorServiceBis.BEHAVIOR_STATE, directAccess = false)
    private String hello;

    @ContextEntity.State.Pull(service = BehaviorServiceBis.class, state = BehaviorServiceBis.BEHAVIOR_STATE)
    private Supplier<String> pull =  () -> {

        return "Coucou from the behavior bis";
    };

    @Override
    public String coucouBis() {
        return hello;
    }
}
