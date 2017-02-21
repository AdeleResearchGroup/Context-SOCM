package fr.liglab.adele.cream.it.behavior.multiple;

import fr.liglab.adele.cream.annotations.behavior.BehaviorProvider;

@BehaviorProvider(contextServices = {BehaviorServiceBis.class,BehaviorService.class})
public class BehaviorMultipleImpl implements BehaviorService,BehaviorServiceBis{
    @Override
    public boolean getTrue() {
        return true;
    }

    @Override
    public boolean getFalse() {
        return false;
    }
}
