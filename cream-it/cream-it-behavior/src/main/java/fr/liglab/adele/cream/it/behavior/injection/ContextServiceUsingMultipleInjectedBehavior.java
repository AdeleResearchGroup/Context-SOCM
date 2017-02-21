package fr.liglab.adele.cream.it.behavior.injection;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.behavior.InjectedBehavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ServiceContextPrime.class)
@Behavior(id="injectedBehavior", spec = BehaviorToInject.class,implem = InjectedBehaviorImpl.class)
@Behavior(id="injectedBehavior2", spec = BehaviorToInjectPrime.class,implem = InjectedBehaviorPrimeImpl.class)
public class ContextServiceUsingMultipleInjectedBehavior implements ServiceContextPrime {

    @InjectedBehavior(id="injectedBehavior")
    BehaviorToInject behaviorToInjected;

    @InjectedBehavior(id="injectedBehavior2")
    BehaviorToInjectPrime behaviorToInjectedPrime;

    @Override
    public boolean returnTrueFromAnInjectedBehavior() {
        return behaviorToInjected.getTrue();
    }

    @Override
    public boolean returnFalseFromAnInjectedBehavior() {
        return behaviorToInjectedPrime.getFalse();
    }
}
