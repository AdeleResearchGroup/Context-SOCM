package fr.liglab.adele.cream.it.behavior.injection;

import fr.liglab.adele.cream.annotations.behavior.BehaviorProvider;

@BehaviorProvider(contextServices = BehaviorToInject.class)
public class InjectedBehaviorImpl implements BehaviorToInject{

    @Override
    public boolean getTrue() {
        return true;
    }
}
