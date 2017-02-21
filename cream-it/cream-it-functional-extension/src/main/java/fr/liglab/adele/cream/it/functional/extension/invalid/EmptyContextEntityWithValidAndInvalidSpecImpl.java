package fr.liglab.adele.cream.it.functional.extension.invalid;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity
@FunctionalExtension(id="validExtension",contextServices = ExtensionValidSpec.class,implementation = ExtensionValidImpl.class)

@FunctionalExtension(id="invalidExtension",contextServices = ExtensionInvalidSpec.class,implementation = ExtenderInvalidImpl.class)
public class EmptyContextEntityWithValidAndInvalidSpecImpl {
}
