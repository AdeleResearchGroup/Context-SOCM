package fr.liglab.adele.cream.it.facilities.test;

import fr.liglab.adele.cream.it.facilities.requirement.*;
import fr.liglab.adele.cream.testing.helpers.ContextBaseTest;
import fr.liglab.adele.cream.testing.helpers.FunctionalExtensionHelper;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class CreamFacilitiesRequirementTest extends ContextBaseTest {

    /**
     * Configuration of test environment
     */

    @Override
    protected List<String> getExtraExports() {
        return Arrays.asList(
                "fr.liglab.adele.cream.it.facilities.requirement"
        );
    }

    @Override
    public boolean deployTestBundle() {
        return true;
    }

    @Override
    public boolean deployCreamRuntimeFacilities() {
        return true;
    }

    @Test
    public void testRequirementWithoutHeritage() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ipojoHelper.createComponentInstance(ContextConsumer.class.getName());
        BindCounterService bindCounter = osgiHelper.getServiceObject(BindCounterService.class);

        ComponentInstance instance = createContextEntity();

        ContextProvideService serviceObj1 = osgiHelper.getServiceObject(ContextProvideService.class);
        Assertions.assertThat(serviceObj1).isNotNull();

        BehaviorService behavior = osgiHelper.waitForService(BehaviorService.class, null, ((long) 2000));

        assertThat(bindCounter.getUnbind()).isEqualTo(0);
        assertThat(bindCounter.getBind()).isEqualTo(1);

        FunctionalExtensionHelper functionalExtensionHelper = contextHelper.getFunctionalExtensionHelper();
        Assertions.assertThat(functionalExtensionHelper.getFunctionalExtension(instance, "behavior1")).isNotNull();

        functionalExtensionHelper.invalidFunctionalExtension(instance, "behavior1");

        assertThat(bindCounter.getUnbind()).isEqualTo(1);
        assertThat(bindCounter.getBind()).isEqualTo(1);

        functionalExtensionHelper.validFunctionalExtension(instance, "behavior1");
        assertThat(bindCounter.getUnbind()).isEqualTo(1);
        assertThat(bindCounter.getBind()).isEqualTo(2);

        bindCounter.callGenericBind();
    }

    @Test
    public void testRequirementWithHeritage() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ipojoHelper.createComponentInstance(ContextConsumer.class.getName());
        BindCounterService bindCounter = osgiHelper.getServiceObject(BindCounterService.class);

        ComponentInstance instance = createContextEntityWithHeritage();

        ContextProvideService serviceObj1 = osgiHelper.getServiceObject(ContextProvideService.class);
        Assertions.assertThat(serviceObj1).isNotNull();

        BehaviorServiceHeritage behavior = osgiHelper.waitForService(BehaviorServiceHeritage.class, null, ((long) 2000));

        assertThat(bindCounter.getUnbind()).isEqualTo(0);
        assertThat(bindCounter.getBind()).isEqualTo(1);

        FunctionalExtensionHelper functionalExtensionHelper = contextHelper.getFunctionalExtensionHelper();
        Assertions.assertThat(functionalExtensionHelper.getFunctionalExtension(instance, "behaviorHeritage")).isNotNull();

        functionalExtensionHelper.invalidFunctionalExtension(instance, "behaviorHeritage");

        assertThat(bindCounter.getUnbind()).isEqualTo(1);
        assertThat(bindCounter.getBind()).isEqualTo(1);

        functionalExtensionHelper.validFunctionalExtension(instance, "behaviorHeritage");
        assertThat(bindCounter.getUnbind()).isEqualTo(1);
        assertThat(bindCounter.getBind()).isEqualTo(2);

        bindCounter.callGenericBind();
    }

    private ComponentInstance createContextEntity() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextProvider.class.getName(), "ContextEntityTest", null);
    }

    private ComponentInstance createContextEntityWithHeritage() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextProviderWithBehaviorHeritage.class.getName(), "ContextEntityTest", null);
    }
}
