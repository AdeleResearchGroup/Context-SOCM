package fr.liglab.adele.cream.it.functional.extension.invalid;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;
import org.apache.felix.ipojo.annotations.Requires;

@FunctionalExtender(contextServices = ExtensionInvalidSpec.class)
public class ExtenderInvalidImpl implements ExtensionInvalidSpec {

    @Requires
    NotExposedService notExposedService;
}
