package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity()
@FunctionalExtension(id = "extensionWithoutParam", contextServices = ExtensionServiceWithoutParameters.class, implementation = ExtensionProviderWithoutParameter.class)

@FunctionalExtension(id = "extensionWithParam", contextServices = ExtensionServiceWithParameters.class, implementation = ExtensionProviderWithParameters.class)
public class EntityMultipleExtensions{



}
