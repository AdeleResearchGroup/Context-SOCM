package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

@FunctionalExtender(contextServices = ExtensionServiceWithAlternativeImplementations.class)
public class ExtenderAlternativeImpl implements ExtensionServiceWithAlternativeImplementations {


    @Override
    public String getExtenderClassName() {
        return ExtenderAlternativeImpl.class.getName();
    }
}
