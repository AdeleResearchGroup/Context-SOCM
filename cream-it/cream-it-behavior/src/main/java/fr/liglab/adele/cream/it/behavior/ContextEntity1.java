package fr.liglab.adele.cream.it.behavior;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ContextService1.class)
@Behavior(id="Behavior1" ,spec = BehaviorSpec1.class,implem = BehviorImpl.class)
public class ContextEntity1 implements ContextService1 {

}
