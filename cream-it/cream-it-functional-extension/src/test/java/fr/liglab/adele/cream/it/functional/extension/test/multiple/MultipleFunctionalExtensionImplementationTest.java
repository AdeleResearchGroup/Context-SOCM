package fr.liglab.adele.cream.it.functional.extension.test.multiple;

import fr.liglab.adele.cream.it.functional.extension.multiple.ContextEntityImpl;
import fr.liglab.adele.cream.it.functional.extension.multiple.ExtensionSpec;
import fr.liglab.adele.cream.it.functional.extension.multiple.ExtensionSpecBis;
import fr.liglab.adele.cream.it.functional.extension.test.FunctionalExtensionBaseCommonConfig;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class MultipleFunctionalExtensionImplementationTest extends FunctionalExtensionBaseCommonConfig {

    @Test
    public void testMulipleImplem() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntityToInit();

        ExtensionSpec serviceObj1 = osgiHelper.getServiceObject(ExtensionSpec.class);

        assertThat(serviceObj1).isNotNull();
        assertThat(serviceObj1 instanceof ExtensionSpecBis).isTrue();
        assertThat(serviceObj1.getFalse()).isFalse();
        assertThat(((ExtensionSpecBis)serviceObj1).getTrue()).isTrue();

        ExtensionSpecBis serviceObjBis = osgiHelper.getServiceObject(ExtensionSpecBis.class);

        assertThat(serviceObjBis).isNotNull();
        assertThat(serviceObjBis instanceof ExtensionSpec).isTrue();
        assertThat(((ExtensionSpec)serviceObjBis).getFalse()).isFalse();
        assertThat(serviceObjBis.getTrue()).isTrue();


    }

    private void createContextEntityToInit() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        contextHelper.getContextEntityHelper().createContextEntity(ContextEntityImpl.class.getName(),"context.entity",new HashMap());
    }

}
