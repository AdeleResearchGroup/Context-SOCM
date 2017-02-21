package fr.liglab.adele.cream.it.behavior.injection;

import fr.liglab.adele.cream.annotations.behavior.*;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ServiceContext.class)
@Behavior(id="injectedBehavior", spec = BehaviorToInject.class,implem = InjectedBehaviorImpl.class)
public class ContextServiceUsingInjectedBehavior implements ServiceContext {

    @InjectedBehavior(id="injectedBehavior")
    BehaviorToInject behaviorToInjected;

    @Override
    public boolean returnTrueFromTheInjectedBehavior() {
        return behaviorToInjected.getTrue();
    }
}
