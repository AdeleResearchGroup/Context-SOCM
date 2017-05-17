package fr.liglab.adele.cream.it.administration.test;

import fr.liglab.adele.cream.administration.api.AdministrationService;
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
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

@ExamReactorStrategy(PerMethod.class)
public class CreamAdministrationTest extends ContextBaseTest {

    private final String CONTEXT_ENTITY_ID = "ContextTestEntity";
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



    @Test
    public void testAdministrationEntityWithParameterOnContextServiceAndBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        AdministrationService adminService  =  osgiHelper.waitForService(AdministrationService.class,null,1000);
        createContextEntity(EntityWithParameterOnContextServiceAndBehavior.class);

        osgiHelper.waitForService(ExtensionServiceWithParameters.class,null,1000);

        Set<ImmutableContextEntity> contextEntities = adminService.getContextEntities();

        assertThat(contextEntities.size()).isEqualTo(1);
        for (ImmutableContextEntity contextEntity : contextEntities){
            checkContextServiceWithParam(contextEntity);

            assertThat(contextEntity.getExtensions().size()).isEqualTo(1);
            for (ImmutableFunctionalExtension functionalExtension:contextEntity.getExtensions()){
                checkExtensionWithParam(functionalExtension);
            }
        }
    }

    @Test
    public void testAdministrationEntityMultipleExtensions() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        AdministrationService adminService  =  osgiHelper.waitForService(AdministrationService.class,null,1000);
        createContextEntity(EntityMultipleExtensions.class);

        osgiHelper.waitForService(ExtensionServiceWithParameters.class,null,1000);
        osgiHelper.waitForService(ExtensionServiceWithoutParameters.class,null,1000);

        Set<ImmutableContextEntity> contextEntities = adminService.getContextEntities();

        assertThat(contextEntities.size()).isEqualTo(1);
        for (ImmutableContextEntity contextEntity : contextEntities){
            checkContextEntityWithNoContextService(contextEntity);

            assertThat(contextEntity.getExtensions().size()).isEqualTo(2);
            for (ImmutableFunctionalExtension functionalExtension:contextEntity.getExtensions()){
                if ("extensionWithParam".equals(functionalExtension.getId())){
                    checkExtensionWithParam(functionalExtension);

                }else if("extensionWithoutParam".equals(functionalExtension.getId())){
                    checkExtensionWithoutParam(functionalExtension);

                }else{
                    fail();
                }

            }
        }
    }

    @Test
    public void testAdministrationEntityOnlyExtension() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        AdministrationService adminService  =  osgiHelper.waitForService(AdministrationService.class,null,1000);
        createContextEntity(EntityOnlyExtension.class);

        osgiHelper.waitForService(ExtensionServiceWithoutParameters.class,null,1000);

        Set<ImmutableContextEntity> contextEntities = adminService.getContextEntities();

        assertThat(contextEntities.size()).isEqualTo(1);
        for (ImmutableContextEntity contextEntity : contextEntities){
            checkContextEntityWithNoContextService(contextEntity);

            assertThat(contextEntity.getExtensions().size()).isEqualTo(1);
            for (ImmutableFunctionalExtension functionalExtension:contextEntity.getExtensions()){
                checkExtensionWithoutParam(functionalExtension);
            }
        }
    }

    @Test
    public void testAdministrationEntityParamOnCsNoParamOnExtension() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        AdministrationService adminService  =  osgiHelper.waitForService(AdministrationService.class,null,1000);
        createContextEntity(EntityParamOnCsNoParamOnExtension.class);

        osgiHelper.waitForService(ExtensionServiceWithoutParameters.class,null,1000);

        Set<ImmutableContextEntity> contextEntities = adminService.getContextEntities();

        assertThat(contextEntities.size()).isEqualTo(1);
        for (ImmutableContextEntity contextEntity : contextEntities){
            checkContextServiceWithParam(contextEntity);

            assertThat(contextEntity.getExtensions().size()).isEqualTo(1);
            for (ImmutableFunctionalExtension functionalExtension:contextEntity.getExtensions()){
                checkExtensionWithoutParam(functionalExtension);
            }
        }
    }

    @Test
    public void testAdministrationEntityParamOnExtensionNoParamOnCs() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        AdministrationService adminService  =  osgiHelper.waitForService(AdministrationService.class,null,1000);
        createContextEntity(EntityParamOnExtensionNoParamOnCs.class);

        osgiHelper.waitForService(ExtensionServiceWithParameters.class,null,1000);

        Set<ImmutableContextEntity> contextEntities = adminService.getContextEntities();

        assertThat(contextEntities.size()).isEqualTo(1);
        for (ImmutableContextEntity contextEntity : contextEntities){
            checkContextServiceWithoutParam(contextEntity);

            assertThat(contextEntity.getExtensions().size()).isEqualTo(1);
            for (ImmutableFunctionalExtension functionalExtension:contextEntity.getExtensions()){
                checkExtensionWithParam(functionalExtension);
            }
        }
    }

    @Test
    public void testAdministrationEntityWithoutExtension() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        AdministrationService adminService  =  osgiHelper.waitForService(AdministrationService.class,null,1000);
        createContextEntity(EntityWithoutExtension.class);

        Set<ImmutableContextEntity> contextEntities = adminService.getContextEntities();

        assertThat(contextEntities.size()).isEqualTo(1);
        for (ImmutableContextEntity contextEntity : contextEntities){
            checkContextServiceWithParam(contextEntity);

            assertThat(contextEntity.getExtensions().size()).isEqualTo(0);

        }
    }

    @Test
    public void testAdministrationReconfiguration() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        AdministrationService adminService  =  osgiHelper.waitForService(AdministrationService.class,null,1000);
        createContextEntity(EntityWithParameterOnContextServiceAndBehavior.class);

        osgiHelper.waitForService(ExtensionServiceWithParameters.class,null,1000);

        /** TEST RECONFIGURATION OF AN ALREADY PERIODIC PARAM **/
        adminService.reconfigureContextEntityFrequency(CONTEXT_ENTITY_ID, ContextEntity.State.id(ContextServiceWithParameters.class,ContextServiceWithParameters.PARAM_4_PERIODICPULL),10,TimeUnit.DAYS);

        Set<ImmutableContextEntity> contextEntities = adminService.getContextEntities();

        assertThat(contextEntities.size()).isEqualTo(1);
        for (ImmutableContextEntity contextEntity : contextEntities){
            for(ImmutableContextState state : contextEntity.getContextStates()){
                String stateId = state.getId();
                if(ContextEntity.State.id(ContextServiceWithParameters.class,"directAccessParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNull();

                }else if (ContextEntity.State.id(ContextServiceWithParameters.class,"pullParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNotNull();
                    assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(-1));
                    assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.SECONDS.toString());

                }else if (ContextEntity.State.id(ContextServiceWithParameters.class,"ApplyParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNull();

                }else if (ContextEntity.State.id(ContextServiceWithParameters.class,"PeriodicPullParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNotNull();
                    assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(10));
                    assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.DAYS.toString());

                }else if (ContextEntity.State.id(ContextServiceWithParameters.class,"ApplyPullParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNotNull();
                    assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(-1));
                    assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.SECONDS.toString());

                }else{
                    fail();
                }
            }
        }

        /** TEST RECONFIGURATION OF A NON PERIODIC PARAM **/
        adminService.reconfigureContextEntityFrequency(CONTEXT_ENTITY_ID, ContextEntity.State.id(ContextServiceWithParameters.class,ContextServiceWithParameters.PARAM_2_PULL),7,TimeUnit.MINUTES);

        Set<ImmutableContextEntity> contextEntitiesReconfigured = adminService.getContextEntities();

        assertThat(contextEntities.size()).isEqualTo(1);
        for (ImmutableContextEntity contextEntity : contextEntitiesReconfigured){
            for(ImmutableContextState state : contextEntity.getContextStates()){
                String stateId = state.getId();
                if(ContextEntity.State.id(ContextServiceWithParameters.class,"directAccessParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNull();

                }else if (ContextEntity.State.id(ContextServiceWithParameters.class,"pullParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNotNull();
                    assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(7));
                    assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.MINUTES.toString());

                }else if (ContextEntity.State.id(ContextServiceWithParameters.class,"ApplyParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNull();

                }else if (ContextEntity.State.id(ContextServiceWithParameters.class,"PeriodicPullParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNotNull();
                    assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(10));
                    assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.DAYS.toString());

                }else if (ContextEntity.State.id(ContextServiceWithParameters.class,"ApplyPullParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNotNull();
                    assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(-1));
                    assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.SECONDS.toString());

                }else{
                    fail();
                }
            }
        }

        /** TEST RECONFIGURATION OF A PARAM IN AN EXTENSION**/
        adminService.reconfigureContextEntityFrequency(CONTEXT_ENTITY_ID, ContextEntity.State.id(ExtensionServiceWithParameters.class,ExtensionServiceWithParameters.PARAM_4_PERIODICPULL),5,TimeUnit.MINUTES);

        Set<ImmutableContextEntity> reconfiguredExtensionContextEntities = adminService.getContextEntities();

        assertThat(contextEntities.size()).isEqualTo(1);
        for (ImmutableContextEntity contextEntity : reconfiguredExtensionContextEntities){
            for(ImmutableContextState state : contextEntity.getContextStates()){
                String stateId = state.getId();
                if(ContextEntity.State.id(ContextServiceWithParameters.class,"directAccessParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNull();

                }else if (ContextEntity.State.id(ContextServiceWithParameters.class,"pullParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNotNull();
                    assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(7));
                    assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.MINUTES.toString());

                }else if (ContextEntity.State.id(ContextServiceWithParameters.class,"ApplyParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNull();

                }else if (ContextEntity.State.id(ContextServiceWithParameters.class,"PeriodicPullParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNotNull();
                    assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(10));
                    assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.DAYS.toString());

                }else if (ContextEntity.State.id(ContextServiceWithParameters.class,"ApplyPullParam").equals(stateId)){
                    assertThat(state.getSynchroPeriod()).isNotNull();
                    assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(-1));
                    assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.SECONDS.toString());

                }else{
                    fail();
                }
            }

            assertThat(contextEntity.getExtensions().size()).isEqualTo(1);
            for (ImmutableFunctionalExtension functionalExtension:contextEntity.getExtensions()){
                for(ImmutableContextState state : functionalExtension.getContextStates()){
                    String stateId = state.getId();
                    if(ContextEntity.State.id(ExtensionServiceWithParameters.class,"directAccessParam").equals(stateId)){
                        assertThat(state.getSynchroPeriod()).isNull();

                    }else if (ContextEntity.State.id(ExtensionServiceWithParameters.class,"pullParam").equals(stateId)){
                        assertThat(state.getSynchroPeriod()).isNotNull();
                        assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(-1));
                        assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.SECONDS.toString());

                    }else if (ContextEntity.State.id(ExtensionServiceWithParameters.class,"ApplyParam").equals(stateId)){
                        assertThat(state.getSynchroPeriod()).isNull();

                    }else if (ContextEntity.State.id(ExtensionServiceWithParameters.class,"PeriodicPullParam").equals(stateId)){
                        assertThat(state.getSynchroPeriod()).isNotNull();
                        assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(5));
                        assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.MINUTES.toString());

                    }else if (ContextEntity.State.id(ExtensionServiceWithParameters.class,"ApplyPullParam").equals(stateId)){
                        assertThat(state.getSynchroPeriod()).isNotNull();
                        assertThat(state.getSynchroPeriod().getPeriod()).isEqualTo(String.valueOf(-1));
                        assertThat(state.getSynchroPeriod().getUnit()).isEqualTo(TimeUnit.SECONDS.toString());

                    }else{
                        fail();
                    }
                }
            }

        }


    }


    private ComponentInstance createContextEntity(Class entityClass) throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(entityClass.getName(), CONTEXT_ENTITY_ID, null);
    }

    private void checkContextEntityWithNoContextService(ImmutableContextEntity contextService){
        assertThat(contextService.getId()).isEqualTo(CONTEXT_ENTITY_ID);
        assertThat(contextService.getContextStates().size()).isEqualTo(0);

        assertThat(contextService.getState()).isEqualTo("valid");

    }

    private void checkContextServiceWithoutParam(ImmutableContextEntity contextService){
        assertThat(contextService.getId()).isEqualTo(CONTEXT_ENTITY_ID);
        assertThat(contextService.getContextStates().size()).isEqualTo(0);

        assertThat(contextService.getState()).isEqualTo("valid");

    }


    private void checkContextServiceWithParam(ImmutableContextEntity contextService){
        assertThat(contextService.getId()).isEqualTo(CONTEXT_ENTITY_ID);
        checkStates(contextService.getContextStates(), ContextServiceWithParameters.class);
    }


    private void checkExtensionWithoutParam(ImmutableFunctionalExtension extensionWithoutParams){
        assertThat(extensionWithoutParams.getId()).isEqualTo("extensionWithoutParam");

        assertThat(extensionWithoutParams.getState()).isEqualTo("valid");

        assertThat(extensionWithoutParams.getSpecifications().size()).isEqualTo(1);
        for (String specification : extensionWithoutParams.getSpecifications()){
            assertThat(specification).isEqualTo(ExtensionServiceWithoutParameters.class.getName());
        }

        assertThat(extensionWithoutParams.getContextStates().size()).isEqualTo(0);
    }

    private void checkExtensionWithParam(ImmutableFunctionalExtension extensionWithParams){
        assertThat(extensionWithParams.getId()).isEqualTo("extensionWithParam");

        assertThat(extensionWithParams.getState()).isEqualTo("valid");

        assertThat(extensionWithParams.getSpecifications().size()).isEqualTo(1);
        for (String specification : extensionWithParams.getSpecifications()){
            assertThat(specification).isEqualTo(ExtensionServiceWithParameters.class.getName());
        }

        checkStates(extensionWithParams.getContextStates(), ExtensionServiceWithParameters.class);
    }

    private void checkStates(List<ImmutableContextState> contextStates,Class contextSpec){
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
