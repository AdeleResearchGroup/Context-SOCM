package fr.liglab.adele.cream.it.facilities.requirement;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity(services = ContextProvideService.class)
@FunctionalExtension(id = "behaviorHeritage", contextServices = BehaviorServiceHeritage.class, implementation = BehaviorHeritageProviderComponent.class)
public class ContextProviderWithBehaviorHeritage implements ContextProvideService {

    @Override
    public boolean getFalse() {
        return false;
    }
}
