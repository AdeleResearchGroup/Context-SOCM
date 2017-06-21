package fr.liglab.adele.cream.it.facilities.requirement;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity(coreServices = ContextProvideService.class)
@FunctionalExtension(id = "behavior1", contextServices = BehaviorService.class, implementation = BehaviorProviderComponent.class)
public class ContextProvider implements ContextProvideService {
    @Override
    public boolean getFalse() {
        return false;
    }
}
