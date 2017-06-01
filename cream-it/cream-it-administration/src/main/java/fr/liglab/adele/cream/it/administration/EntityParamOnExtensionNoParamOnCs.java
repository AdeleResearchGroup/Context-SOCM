package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity(services = ContextServiceWithoutParameters.class)
@FunctionalExtension(id = "extensionWithParam", contextServices = ExtensionServiceWithParameters.class, implementation = ExtenderWithParameters.class)
public class EntityParamOnExtensionNoParamOnCs implements ContextServiceWithoutParameters{

    @Override
    public boolean getFalse() {
        return false;
    }

}
