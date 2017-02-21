package fr.liglab.adele.cream.it.functional.extension.synchronisation;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ContextService1.class)
@FunctionalExtension(id = "behaviorWithHeritage", contextServices = ExtensionSpecToExtends.class,implementation = ExtenderImplemAbstractExtension.class)
public class ContextEntityWithFunctionalExtensionHeritage implements ContextService1{
}
