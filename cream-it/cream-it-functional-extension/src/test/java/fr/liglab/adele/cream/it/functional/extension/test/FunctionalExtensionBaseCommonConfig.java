package fr.liglab.adele.cream.it.functional.extension.test;

import fr.liglab.adele.cream.testing.helpers.ContextBaseTest;

import java.util.Arrays;
import java.util.List;

/**
 * Created by aygalinc on 26/08/16.
 */
public abstract class FunctionalExtensionBaseCommonConfig extends ContextBaseTest {

    @Override
    protected List<String> getExtraExports() {
        return Arrays.asList(
                "fr.liglab.adele.cream.it.functional.extension.injection",
                "fr.liglab.adele.cream.it.functional.extension.synchronisation",
                "fr.liglab.adele.cream.it.functional.extension.changeOn",
                "fr.liglab.adele.cream.it.functional.extension.multiple",
                "fr.liglab.adele.cream.it.functional.extension.invalid",
                "fr.liglab.adele.cream.it.functional.extension.contextSource"
        );
    }


    @Override
    public boolean deployTestBundle() {
        return true;
    }
    
}
