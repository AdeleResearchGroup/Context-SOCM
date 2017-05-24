package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity()
@FunctionalExtension(mandatory = true,id = "extensionWithoutParam", contextServices = ExtensionServiceWithoutParameters.class, implementation = ExtenderWithoutParameter.class)

@FunctionalExtension(id = "extensionWithParam", contextServices = ExtensionServiceWithParameters.class, implementation = ExtenderWithParameters.class)
public class EntityMultipleExtensions{



}
