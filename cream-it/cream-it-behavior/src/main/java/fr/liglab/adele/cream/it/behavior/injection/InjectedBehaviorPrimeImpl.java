package fr.liglab.adele.cream.it.behavior.injection;

import fr.liglab.adele.cream.annotations.behavior.BehaviorProvider;

@BehaviorProvider(spec = BehaviorToInjectPrime.class)
public class InjectedBehaviorPrimeImpl implements BehaviorToInjectPrime{

    @Override
    public boolean getFalse() {
        return false;
    }
}
