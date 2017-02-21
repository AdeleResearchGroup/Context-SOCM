package fr.liglab.adele.cream.it.facilities.requirement;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ContextProvideService.class)
@Behavior(id="behavior1",contextServices = BehaviorService.class,implem = BehaviorProviderComponent.class)
public class ContextProvider implements ContextProvideService {
    @Override
    public boolean getFalse() {
        return false;
    }
}
