package fr.liglab.adele.cream.it.functional.extension.contextSource;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

@FunctionalExtender(contextServices = ExtensionSpec1.class)
public class FunctionalExtenderImpl1 implements ExtensionSpec1 {

    @ContextEntity.State.Field(service = ExtensionSpec1.class,state = EXTENSION_STATE,directAccess = true)
    String stateField;

    @Override
    public void setValue(String filterValue) {
        stateField = filterValue;
    }
}
