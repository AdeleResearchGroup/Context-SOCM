package fr.liglab.adele.icasa.context.model.example;


import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

import java.util.function.Supplier;

@FunctionalExtender(contextServices = BehaviorService.class)
public class BehaviorImpl implements BehaviorService {

    @ContextEntity.State.Field(service = BehaviorService.class, state = BehaviorService.BEHAVIOR_STATE, directAccess = false)
    private String hello;

    @ContextEntity.State.Pull(service = BehaviorService.class, state = BehaviorService.BEHAVIOR_STATE)
    private Supplier<String> pull = () -> {

        return "Coucou from the behavior";
    };

    @Override
    public String coucou() {
        return hello;
    }
}
