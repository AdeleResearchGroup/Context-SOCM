package fr.liglab.adele.cream.it.functional.extension.synchronisation;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity(services = ContextService1.class)
@FunctionalExtension(id="BehaviorToInit" , contextServices = ExtensionInitValue.class,implementation = ExtenderInitValueImpl.class)
public class ContextEntityWithBehaviorToInit implements ContextService1 {

}
