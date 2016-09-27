package fr.liglab.adele.cream.it.behavior.contextSource;

import fr.liglab.adele.cream.annotations.behavior.BehaviorProvider;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@BehaviorProvider(spec = BehaviorService1.class)
public class BehaviorImpl1 implements BehaviorService1 {

    @ContextEntity.State.Field(service = BehaviorService1.class,state = BehaviorService1.BEHAVIOR_STATE,directAccess = true)
    String stateField;

    @Override
    public void setValue(String filterValue) {
        stateField = filterValue;
    }
}
