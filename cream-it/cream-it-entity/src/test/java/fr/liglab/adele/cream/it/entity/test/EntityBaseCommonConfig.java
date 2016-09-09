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
        if ("Linux".equalsIgnoreCase(System.getProperty("os.name"))){
            return Arrays.asList(
                    "fr.liglab.adele.cream.it.entity.synchronisation"
                    //By convention services package are exported by the base test
            );
        } else {
            return Arrays.asList(
                    "fr.liglab.adele.cream.it.entity.synchronisation",
                    "fr.liglab.adele.cream.it.entity.services"
                    //By convention services package are exported by the base test but FAILED on windows....
            );
        }

    }


    @Override
    public boolean deployTestBundle() {
        return true;
    }
}
