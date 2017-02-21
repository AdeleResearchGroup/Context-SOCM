package fr.liglab.adele.cream.it.functional.extension.synchronisation;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity(services = ContextService1.class)
@FunctionalExtension(id = "Behavior1", contextServices = ExtensionSpec1.class, implementation = ExtensionSpec1Impl.class)
@FunctionalExtension(id = "Behavior2", contextServices = FunctionalExtension2.class, implementation = FunctionalExtension2Impl.class)
public class ContextEntity2 implements ContextService1 {

}
