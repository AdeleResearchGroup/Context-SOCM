package fr.liglab.adele.cream.it.administration;


import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity()
@FunctionalExtension(id = "ExtensionWithAlternativeImplementations", contextServices = ExtensionServiceWithAlternativeImplementations.class, implementation = ExtenderAlternativeImpl.class)

public class EntityWithPossibleAlternativeExtensions {
}
