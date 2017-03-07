package fr.liglab.adele.cream.it.functional.extension.multiple;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity(services = {})
@FunctionalExtension(id = "behavior1", contextServices = {ExtensionSpec.class, ExtensionSpecBis.class}, implementation = ExtenderMultipleExtensionImpl.class)
public class ContextEntityImpl {

}
