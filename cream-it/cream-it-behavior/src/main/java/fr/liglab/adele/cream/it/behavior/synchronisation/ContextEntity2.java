package fr.liglab.adele.cream.it.behavior.synchronisation;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ContextService1.class)
@Behavior(id="Behavior1" , contextServices = BehaviorSpec1.class,implem = BehaviorSpec1Impl.class)
@Behavior(id="Behavior2" , contextServices = BehaviorSpec2.class,implem = BehaviorSpec2Impl.class)
public class ContextEntity2 implements ContextService1 {

}
