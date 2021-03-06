package fr.liglab.adele.cream.it.functional.extension.synchronisation;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity(coreServices = ContextService1.class)
@FunctionalExtension(id = "Behavior1", contextServices = ExtensionSpec1.class, implementation = ExtensionSpec1Impl.class)
public class ContextEntity1 implements ContextService1 {

}
