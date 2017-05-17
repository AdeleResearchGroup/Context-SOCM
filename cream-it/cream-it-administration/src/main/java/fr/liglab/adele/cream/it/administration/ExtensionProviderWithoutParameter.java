package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

@FunctionalExtender(contextServices = ExtensionServiceWithoutParameters.class)
public class ExtensionProviderWithoutParameter implements ExtensionServiceWithoutParameters {
    @Override
    public boolean getTrue() {
        return true;
    }



}
