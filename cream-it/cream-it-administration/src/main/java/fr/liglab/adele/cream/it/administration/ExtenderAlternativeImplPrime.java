package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

@FunctionalExtender(contextServices = ExtensionServiceWithAlternativeImplementations.class)
public class ExtenderAlternativeImplPrime implements ExtensionServiceWithAlternativeImplementations {


    @Override
    public String getExtenderClassName() {
        return ExtenderAlternativeImplPrime.class.getName();
    }
}
