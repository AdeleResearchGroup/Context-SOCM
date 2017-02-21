package fr.liglab.adele.cream.it.functional.extension.synchronisation;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

@FunctionalExtender(contextServices = ExtensionInitValue.class)
public class ExtenderInitValueImpl implements ExtensionInitValue {

    @ContextEntity.State.Field(service = ExtensionInitValue.class,state = ExtensionInitValue.PARAM_TO_INIT)
    public boolean paramToInit;

    @Override
    public boolean returnInitValue() {
        return paramToInit;
    }
}
