package fr.liglab.adele.cream.it.behavior.synchronisation;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ContextService1.class)
@Behavior(id = "behaviorWithHeritage", contextServices = BehaviorSpecToExtends.class,implem = BehaviorImplementAbstractBehavior.class)
public class ContextEntityWithBehaviorHeritage implements ContextService1{
}
