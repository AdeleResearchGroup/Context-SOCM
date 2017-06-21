package fr.liglab.adele.cream.it.administration.test;

import fr.liglab.adele.cream.administration.api.ImmutableContextEntity;
import fr.liglab.adele.cream.administration.api.ImmutableContextState;
import fr.liglab.adele.cream.administration.api.ImmutableFunctionalExtension;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.it.administration.*;
import fr.liglab.adele.cream.testing.helpers.ContextBaseTest;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * Created by aygalinc on 24/05/17.
 */
public abstract class  CreamAdministrationBaseTest  extends ContextBaseTest {

    protected final String CONTEXT_ENTITY_ID = "ContextTestEntity";
    /**
     * Configuration of test environment
     */

    @Override
    protected List<String> getExtraExports() {
        return Arrays.asList(
                "fr.liglab.adele.cream.it.administration"
        );
    }

    @Override
    public boolean deployTestBundle() {
        return true;
    }

    @Override
    public boolean deployCreamAdministration() {
        return true;
    }

    protected ComponentInstance createContextEntity(Class entityClass) throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(entityClass.getName(), CONTEXT_ENTITY_ID, null);
    }

    protected void checkContextEntityWithNoContextService(ImmutableContextEntity contextService){
        assertThat(contextService.getId()).isEqualTo(CONTEXT_ENTITY_ID);
        assertThat(contextService.getCore()).isNotNull();

        assertThat(contextService.getCore().getContextStates().size()).isEqualTo(0);

        assertThat(contextService.getState()).isEqualTo("valid");

        assertThat(contextService.getCore().getImplementedSpecifications().size()).isEqualTo(0);


    }

    protected void checkContextServiceWithoutParam(ImmutableContextEntity contextService){
        assertThat(contextService.getId()).isEqualTo(CONTEXT_ENTITY_ID);
        assertThat(contextService.getCore()).isNotNull();

        assertThat(contextService.getCore().getContextStates().size()).isEqualTo(0);

        assertThat(contextService.getState()).isEqualTo("valid");

        assertThat(contextService.getCore().getImplementedSpecifications().size()).isEqualTo(1);
        for (String specification : contextService.getCore().getImplementedSpecifications()){
            assertThat(specification).isEqualTo(ContextServiceWithoutParameters.class.getName());
        }


    }


    protected void checkContextServiceWithParam(ImmutableContextEntity contextService){
        assertThat(contextService.getId()).isEqualTo(CONTEXT_ENTITY_ID);
        assertThat(contextService.getCore()).isNotNull();

        checkStates(contextService.getCore().getContextStates(), ContextServiceWithParameters.class);

        assertThat(contextService.getState()).isEqualTo("valid");

        assertThat(contextService.getCore().getImplementedSpecifications().size()).isEqualTo(1);
        for (String specification : contextService.getCore().getImplementedSpecifications()){
            assertThat(specification).isEqualTo(ContextServiceWithParameters.class.getName());
        }
    }


    protected void checkExtensionWithoutParam(ImmutableFunctionalExtension extensionWithoutParams){
        assertThat(extensionWithoutParams.getId()).isEqualTo("extensionWithoutParam");

        assertThat(extensionWithoutParams.getState()).isEqualTo("valid");

        assertThat(extensionWithoutParams.isInstantiate()).isEqualTo("true");

        assertThat(extensionWithoutParams.isMandatory()).isEqualTo("true");

        assertThat(extensionWithoutParams.getAlternativeConfigurations().size()).isEqualTo(1);

        assertThat(extensionWithoutParams.getManagedSpecifications().size()).isEqualTo(1);
        for (String specification : extensionWithoutParams.getManagedSpecifications()){
            assertThat(specification).isEqualTo(ExtensionServiceWithoutParameters.class.getName());
        }

        assertThat(extensionWithoutParams.getImplementedSpecifications().size()).isEqualTo(1);
        for (String specification : extensionWithoutParams.getImplementedSpecifications()){
            assertThat(specification).isEqualTo(ExtensionServiceWithoutParameters.class.getName());
        }

        assertThat(extensionWithoutParams.getContextStates().size()).isEqualTo(0);
    }

    protected void checkExtensionWithParam(ImmutableFunctionalExtension extensionWithParams){
        assertThat(extensionWithParams.getId()).isEqualTo("extensionWithParam");

        assertThat(extensionWithParams.isInstantiate()).isEqualTo("true");


        assertThat(extensionWithParams.isMandatory()).isEqualTo("false");

        assertThat(extensionWithParams.getState()).isEqualTo("valid");

        assertThat(extensionWithParams.getAlternativeConfigurations().size()).isEqualTo(1);

        assertThat(extensionWithParams.getManagedSpecifications().size()).isEqualTo(1);
        for (String specification : extensionWithParams.getManagedSpecifications()){
            assertThat(specification).isEqualTo(ExtensionServiceWithParameters.class.getName());
        }

        assertThat(extensionWithParams.getImplementedSpecifications().size()).isEqualTo(1);
        for (String specification : extensionWithParams.getImplementedSpecifications()){
            assertThat(specification).isEqualTo(ExtensionServiceWithParameters.class.getName());
        }

        checkStates(extensionWithParams.getContextStates(), ExtensionServiceWithParameters.class);
    }

    protected void checkAlternativeExtension(ImmutableFunctionalExtension extensionWithParams){
        assertThat(extensionWithParams.getId()).isEqualTo("ExtensionWithAlternativeImplementations");

        assertThat(extensionWithParams.isInstantiate()).isEqualTo("true");

        assertThat(extensionWithParams.isMandatory()).isEqualTo("false");

        assertThat(extensionWithParams.getState()).isEqualTo("valid");

        assertThat(extensionWithParams.getAlternativeConfigurations().size()).isEqualTo(2);

        assertThat(extensionWithParams.getManagedSpecifications().size()).isEqualTo(1);

        assertThat(extensionWithParams.getImplementedSpecifications().size()).isEqualTo(1);

        for (String specification : extensionWithParams.getImplementedSpecifications()){
            assertThat(specification).isEqualTo(ExtensionServiceWithAlternativeImplementations.class.getName());
        }


    }

    protected void checkStates(List<ImmutableContextState> contextStates, Class contextSpec){
        assertThat(contextStates.size()).isEqualTo(5);
        for (ImmutableContextState state:contextStates){
            String stateId = state.getId();
            if(ContextEntity.State.id(contextSpec,"directAccessParam").equals(stateId)){
                assertThat(state.getSynchroPeriod()).isNull();

            }else if (ContextEntity.State.id(contextSpec,"pullParam").equals(stateId)){
                assertThat(state.getSynchroPeriod()).isNotNull();
                assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(-1));
                assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.SECONDS.toString());

            }else if (ContextEntity.State.id(contextSpec,"ApplyParam").equals(stateId)){
                assertThat(state.getSynchroPeriod()).isNull();

            }else if (ContextEntity.State.id(contextSpec,"PeriodicPullParam").equals(stateId)){
                assertThat(state.getSynchroPeriod()).isNotNull();
                assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(2));
                assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.SECONDS.toString());

            }else if (ContextEntity.State.id(contextSpec,"ApplyPullParam").equals(stateId)){
                assertThat(state.getSynchroPeriod()).isNotNull();
                assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(-1));
                assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.SECONDS.toString());

            }else{
                fail();
            }
        }
    }
}
