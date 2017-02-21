package fr.liglab.adele.cream.it.behavior.multiple;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = {})
@Behavior(id="behavior1",contextServices = {BehaviorService.class,BehaviorServiceBis.class},implem = BehaviorMultipleImpl.class)
public class ContextEntityImpl {

}
