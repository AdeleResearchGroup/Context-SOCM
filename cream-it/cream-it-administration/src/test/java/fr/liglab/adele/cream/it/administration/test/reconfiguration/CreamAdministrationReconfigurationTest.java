package fr.liglab.adele.cream.it.administration.test.reconfiguration;

import fr.liglab.adele.cream.administration.api.AdministrationService;
import fr.liglab.adele.cream.administration.api.ImmutableContextEntity;
import fr.liglab.adele.cream.administration.api.ImmutableContextState;
import fr.liglab.adele.cream.administration.api.ImmutableFunctionalExtension;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.it.administration.*;
import fr.liglab.adele.cream.it.administration.test.CreamAdministrationBaseTest;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

@ExamReactorStrategy(PerMethod.class)
public class CreamAdministrationReconfigurationTest extends CreamAdministrationBaseTest {

    @Test
    public void testAdministrationReconfigurationFrequency() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
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

    @Test
    public void testReconfigurationComposition() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        AdministrationService adminService  =  osgiHelper.waitForService(AdministrationService.class,null,1000);
        createContextEntity(EntityWithPossibleAlternativeExtensions.class);

        osgiHelper.waitForService(ExtensionServiceWithAlternativeImplementations.class,null,1000);

        adminService.reconfigureContextEntityComposition(CONTEXT_ENTITY_ID,"ExtensionWithAlternativeImplementations",ExtenderAlternativeImplPrime.class.getName());

        ExtensionServiceWithAlternativeImplementations entityWithPossibleAlternativeExtensions = osgiHelper.waitForService(ExtensionServiceWithAlternativeImplementations.class,null,1000);

        Set<ImmutableContextEntity> contextEntities = adminService.getContextEntities();

        assertThat(contextEntities.size()).isEqualTo(1);
        for (ImmutableContextEntity contextEntity : contextEntities){

            assertThat(contextEntity.getExtensions().size()).isEqualTo(1);
            for (ImmutableFunctionalExtension functionalExtension:contextEntity.getExtensions()){
                checkAlternativeExtension(functionalExtension);
            }
        }

        assertThat(entityWithPossibleAlternativeExtensions.getExtenderClassName()).isEqualTo(ExtenderAlternativeImplPrime.class.getName());
    }
}
