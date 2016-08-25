package fr.liglab.adele.cream.it.test.synchronisation;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.it.behavior.synchronisation.BehaviorInitValue;
import fr.liglab.adele.cream.it.behavior.synchronisation.ContextEntity2;
import fr.liglab.adele.cream.it.behavior.synchronisation.ContextEntityWithBehaviorToInit;
import fr.liglab.adele.cream.it.behavior.synchronisation.ContextService1;
import fr.liglab.adele.cream.testing.helpers.ContextBaseTest;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by aygalinc on 25/08/16.
 */
public class InitialisationBehaviorTest  extends ContextBaseTest {

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



    @Test
    public void testInitBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntityToInit("InitWithTrue",true);

        Object serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);
        BehaviorInitValue behaviorInitValue1 = osgiHelper.getServiceObject(BehaviorInitValue.class);

        assertThat(serviceObj1).isNotNull();
        assertThat(behaviorInitValue1).isNotNull();

        assertThat(behaviorInitValue1.returnInitValue()).isTrue();

        createContextEntityToInit("InitWithFalse",false);

        Object serviceObj2 = osgiHelper.getServiceObject(ContextService1.class);
        BehaviorInitValue behaviorInitValue2 = osgiHelper.getServiceObject(BehaviorInitValue.class);

        assertThat(serviceObj2).isNotNull();
        assertThat(behaviorInitValue2).isNotNull();

        assertThat(behaviorInitValue2.returnInitValue()).isFalse();

    }

    private void createContextEntityToInit(String name,boolean initValue) throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        Map<String,Object> properties = new HashMap<>();
        properties.put(ContextEntity.State.ID(BehaviorInitValue.class,BehaviorInitValue.PARAM_TO_INIT),initValue);
        contextHelper.getContextEntityHelper().createContextEntity(ContextEntityWithBehaviorToInit.class.getName(),name,properties);
    }
}
