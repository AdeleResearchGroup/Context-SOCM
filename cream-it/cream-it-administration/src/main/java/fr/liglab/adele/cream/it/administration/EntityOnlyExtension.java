package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ContextEntity()
@FunctionalExtension(id = "extensionWithoutParam", contextServices = ExtensionServiceWithoutParameters.class, implementation = ExtensionProviderWithoutParameter.class)
public class EntityOnlyExtension{

}
