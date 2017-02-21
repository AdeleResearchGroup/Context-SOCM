package fr.liglab.adele.cream.it.functional.extension.test.departure;

import fr.liglab.adele.cream.it.functional.extension.invalid.EmptyContextEntityWithInvalidSpecImpl;
import fr.liglab.adele.cream.it.functional.extension.invalid.EmptyContextEntityWithValidAndInvalidSpecImpl;
import fr.liglab.adele.cream.it.functional.extension.invalid.ExtensionInvalidSpec;
import fr.liglab.adele.cream.it.functional.extension.invalid.ExtensionValidSpec;
import fr.liglab.adele.cream.it.functional.extension.test.FunctionalExtensionBaseCommonConfig;
import fr.liglab.adele.cream.testing.helpers.FunctionalExtensionHelper;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by aygalinc on 21/02/17.
 */
public class FunctionalExtensionInvalidTest extends FunctionalExtensionBaseCommonConfig {

    @Test
    public void testInvalidExtension() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createEmptyContextEntityImpl();

        FunctionalExtensionHelper functionalExtensionHelper = contextHelper.getFunctionalExtensionHelper();
        /** Check if extension is invalid because requires not satisfy and context entity is valid */
        assertThat(functionalExtensionHelper.getBehavior(instance,"extension")).isNotNull();
        assertThat(functionalExtensionHelper.getBehavior(instance,"extension").getState()).isEqualTo(ComponentInstance.INVALID);
        assertThat(instance.getState()).isEqualTo(ComponentInstance.VALID);

        ExtensionInvalidSpec extensionInvalidSpec = osgiHelper.waitForService(ExtensionInvalidSpec.class,null,((long)2000),false);
        assertThat(extensionInvalidSpec).isNull();



    }

    @Test
    public void testValidAndInvalidExtension() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createEmptyContextEntityImplWithInvalidAndValid();

        FunctionalExtensionHelper functionalExtensionHelper = contextHelper.getFunctionalExtensionHelper();
        /** Check if extension is invalid because requires not satisfy and context entity is valid */
        assertThat(functionalExtensionHelper.getBehavior(instance,"invalidExtension")).isNotNull();
        assertThat(functionalExtensionHelper.getBehavior(instance,"invalidExtension").getState()).isEqualTo(ComponentInstance.INVALID);
        assertThat(functionalExtensionHelper.getBehavior(instance,"validExtension")).isNotNull();
        assertThat(functionalExtensionHelper.getBehavior(instance,"validExtension").getState()).isEqualTo(ComponentInstance.VALID);

        assertThat(instance.getState()).isEqualTo(ComponentInstance.VALID);


        ExtensionInvalidSpec extensionInvalidSpec = osgiHelper.waitForService(ExtensionInvalidSpec.class,null,((long)2000),false);
        assertThat(extensionInvalidSpec).isNull();

        ExtensionValidSpec extensionValidSpec = osgiHelper.waitForService(ExtensionValidSpec.class,null,((long)2000),true);
        assertThat(extensionValidSpec).isNotNull();

    }

    private ComponentInstance createEmptyContextEntityImpl() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(EmptyContextEntityWithInvalidSpecImpl.class.getName(),"ContextEntityTest",null);
    }

    private ComponentInstance createEmptyContextEntityImplWithInvalidAndValid() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(EmptyContextEntityWithValidAndInvalidSpecImpl.class.getName(),"ContextEntityTest",null);
    }
}
