package fr.liglab.adele.cream.it.functional.extension.multiple;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

@FunctionalExtender(contextServices = {ExtensionSpecBis.class, ExtensionSpec.class})
public class ExtenderMultipleExtensionImpl implements ExtensionSpec, ExtensionSpecBis {
    @Override
    public boolean getTrue() {
        return true;
    }

    @Override
    public boolean getFalse() {
        return false;
    }
}
