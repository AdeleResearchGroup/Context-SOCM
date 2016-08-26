package fr.liglab.adele.cream.it.behavior.test;

import fr.liglab.adele.cream.testing.helpers.ContextBaseTest;

import java.util.Arrays;
import java.util.List;

/**
 * Created by aygalinc on 26/08/16.
 */
public abstract class BehaviorBaseCommonConfig extends ContextBaseTest {

    @Override
    protected List<String> getExtraExports() {
        return Arrays.asList(
                "fr.liglab.adele.cream.it.behavior.injection",
                "fr.liglab.adele.cream.it.behavior.synchronisation"
        );
    }


    @Override
    public boolean deployTestBundle() {
        return true;
    }
}
