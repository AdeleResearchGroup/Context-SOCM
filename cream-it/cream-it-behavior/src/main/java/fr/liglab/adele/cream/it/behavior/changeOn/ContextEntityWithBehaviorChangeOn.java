package fr.liglab.adele.cream.it.behavior.changeOn;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ServiceOfContext.class)
@Behavior(contextServices = BehaviorService.class,implem = BehaviorImpl.class,id="changOnBehavior")
public class ContextEntityWithBehaviorChangeOn implements ServiceOfContext{

    @fr.liglab.adele.cream.annotations.entity.ContextEntity.State.Field(service = ServiceOfContext.class,state = ServiceOfContext.STATE_1,directAccess = true)
    private boolean state1;

    @Override
    public void setState1(boolean newState) {
        state1 = newState;
    }
}
