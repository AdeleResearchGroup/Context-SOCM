package fr.liglab.adele.cream.it.entity.test;

import fr.liglab.adele.cream.testing.helpers.ContextBaseTest;

import java.util.Arrays;
import java.util.List;

/**
 * Created by aygalinc on 26/08/16.
 */
public abstract class EntityBaseCommonConfig extends ContextBaseTest {


    @Override
    protected List<String> getExtraExports() {
        return Arrays.asList(
                "fr.liglab.adele.cream.it.entity.synchronisation"
                //By convention services package are exported by the base test
        );
    }


    @Override
    public boolean deployTestBundle() {
        return true;
    }
}
