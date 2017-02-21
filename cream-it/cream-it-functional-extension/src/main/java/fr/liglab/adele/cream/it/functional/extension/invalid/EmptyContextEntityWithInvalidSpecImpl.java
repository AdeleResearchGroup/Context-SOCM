package fr.liglab.adele.cream.it.functional.extension.invalid;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity
@FunctionalExtension(id="extension",contextServices = ExtensionInvalidSpec.class,implementation = ExtenderInvalidImpl.class)
public class EmptyContextEntityWithInvalidSpecImpl {
}
