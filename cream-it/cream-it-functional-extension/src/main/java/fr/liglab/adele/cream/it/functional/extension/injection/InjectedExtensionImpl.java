package fr.liglab.adele.cream.it.functional.extension.injection;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

@FunctionalExtender(contextServices = ExtensionToInject.class)
public class InjectedExtensionImpl implements ExtensionToInject {

    @Override
    public boolean getTrue() {
        return true;
    }
}
