package fr.liglab.adele.cream.it.behavior.test.synchronisation;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.it.behavior.synchronisation.BehaviorInitValue;
import fr.liglab.adele.cream.it.behavior.synchronisation.ContextEntityWithBehaviorToInit;
import fr.liglab.adele.cream.it.behavior.synchronisation.ContextService1;
import fr.liglab.adele.cream.it.behavior.test.BehaviorBaseCommonConfig;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class InitialisationBehaviorTest  extends BehaviorBaseCommonConfig {

    @Test
    public void testInitBehaviorWithTrue() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntityToInit("InitWithTrue",true);

        Object serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);
        BehaviorInitValue behaviorInitValue1 = osgiHelper.getServiceObject(BehaviorInitValue.class);

        assertThat(serviceObj1).isNotNull();
        assertThat(behaviorInitValue1).isNotNull();
        assertThat(behaviorInitValue1.returnInitValue()).isTrue();
    }

    @Test
    public void testInitBehaviorWithFalse() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntityToInit("InitWithFalse",false);

        Object serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);
        BehaviorInitValue behaviorInitValue1 = osgiHelper.getServiceObject(BehaviorInitValue.class);

        assertThat(serviceObj1).isNotNull();
        assertThat(behaviorInitValue1).isNotNull();

        assertThat(behaviorInitValue1.returnInitValue()).isFalse();

    }

    private void createContextEntityToInit(String name,boolean initValue) throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        Map<String,Object> properties = new HashMap<>();
        properties.put(ContextEntity.State.id(BehaviorInitValue.class,BehaviorInitValue.PARAM_TO_INIT),initValue);
        contextHelper.getContextEntityHelper().createContextEntity(ContextEntityWithBehaviorToInit.class.getName(),name,properties);
    }
}
