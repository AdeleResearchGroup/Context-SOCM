package fr.liglab.adele.cream.it.functional.extension.injection;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

@FunctionalExtender(contextServices = ExtensionToInjectPrime.class)
public class InjectedExtensionPrimeImpl implements ExtensionToInjectPrime {

    @Override
    public boolean getFalse() {
        return false;
    }
}
