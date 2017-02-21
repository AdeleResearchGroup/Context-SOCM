package fr.liglab.adele.cream.it.behavior.synchronisation;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ContextService1.class)
@Behavior(id="BehaviorToInit" , spec = BehaviorInitValue.class,implem = BehaviorInitValueImpl.class)
public class ContextEntityWithBehaviorToInit implements ContextService1 {

}
