package fr.liglab.adele.cream.it.facilities.requirement;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ContextProvideService.class)
@Behavior(id="behaviorHeritage",contextServices = BehaviorServiceHeritage.class,implem = BehaviorHeritageProviderComponent.class)
public class ContextProviderWithBehaviorHeritage implements ContextProvideService {

    @Override
    public boolean getFalse() {
        return false;
    }
}
