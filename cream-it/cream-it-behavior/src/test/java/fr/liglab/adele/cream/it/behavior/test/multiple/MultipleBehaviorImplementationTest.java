package fr.liglab.adele.cream.it.behavior.test.multiple;

import fr.liglab.adele.cream.it.behavior.multiple.BehaviorService;
import fr.liglab.adele.cream.it.behavior.multiple.BehaviorServiceBis;
import fr.liglab.adele.cream.it.behavior.multiple.ContextEntityImpl;
import fr.liglab.adele.cream.it.behavior.test.BehaviorBaseCommonConfig;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class MultipleBehaviorImplementationTest  extends BehaviorBaseCommonConfig {

    @Test
    public void testMulipleImplem() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntityToInit();

        BehaviorService serviceObj1 = osgiHelper.getServiceObject(BehaviorService.class);

        assertThat(serviceObj1).isNotNull();
        assertThat(serviceObj1 instanceof BehaviorServiceBis).isTrue();
        assertThat(serviceObj1.getFalse()).isFalse();
        assertThat(((BehaviorServiceBis)serviceObj1).getTrue()).isTrue();

        BehaviorServiceBis serviceObjBis = osgiHelper.getServiceObject(BehaviorServiceBis.class);

        assertThat(serviceObjBis).isNotNull();
        assertThat(serviceObjBis instanceof BehaviorService).isTrue();
        assertThat(((BehaviorService)serviceObjBis).getFalse()).isFalse();
        assertThat(serviceObjBis.getTrue()).isTrue();


    }

    private void createContextEntityToInit() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        contextHelper.getContextEntityHelper().createContextEntity(ContextEntityImpl.class.getName(),"context.entity",new HashMap());
    }

}
