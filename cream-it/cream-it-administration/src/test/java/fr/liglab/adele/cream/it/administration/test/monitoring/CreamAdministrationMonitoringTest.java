package fr.liglab.adele.cream.it.administration.test.monitoring;

import fr.liglab.adele.cream.administration.api.AdministrationService;
import fr.liglab.adele.cream.administration.api.ImmutableContextEntity;
import fr.liglab.adele.cream.administration.api.ImmutableFunctionalExtension;
import fr.liglab.adele.cream.administration.api.ImmutableRelation;
import fr.liglab.adele.cream.it.administration.*;
import fr.liglab.adele.cream.it.administration.test.CreamAdministrationBaseTest;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

@ExamReactorStrategy(PerMethod.class)
public class CreamAdministrationMonitoringTest extends CreamAdministrationBaseTest {


    @Test
    public void testDependencies() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        AdministrationService adminService  =  osgiHelper.waitForService(AdministrationService.class,null,1000);

        contextHelper.getContextEntityHelper().createContextEntity(EntityWithoutExtension.class.getName(), "ServiceProvider", null);
        ipojoHelper.createComponentInstance(RegularIPojoServiceProvider.class.getName());
        createContextEntity(EntityWithDependencies.class);

        ImmutableContextEntity contextEntity = adminService.getContextEntity(CONTEXT_ENTITY_ID);

        List<ImmutableRelation> relations = contextEntity.getCore().getRelations();
        assertThat(relations).isNotNull();
        assertThat(relations.size()).isEqualTo(1);
        for (ImmutableRelation relation : relations){
            assertThat(relation.getSourcesId().size()).isEqualTo(1);
            for (String sourceID : relation.getSourcesId()){
                assertThat(sourceID).isEqualTo("ServiceProvider");
            }
        }
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
    public void testAdministrationEntityWithPossibleAlternativeExtensions() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        AdministrationService adminService  =  osgiHelper.waitForService(AdministrationService.class,null,1000);
        createContextEntity(EntityWithPossibleAlternativeExtensions.class);

        ExtensionServiceWithAlternativeImplementations entityWithPossibleAlternativeExtensions = osgiHelper.waitForService(ExtensionServiceWithAlternativeImplementations.class,null,1000);

        Set<ImmutableContextEntity> contextEntities = adminService.getContextEntities();

        assertThat(contextEntities.size()).isEqualTo(1);
        for (ImmutableContextEntity contextEntity : contextEntities){

            assertThat(contextEntity.getExtensions().size()).isEqualTo(1);
            for (ImmutableFunctionalExtension functionalExtension:contextEntity.getExtensions()){
                checkAlternativeExtension(functionalExtension);
            }
        }

        assertThat(entityWithPossibleAlternativeExtensions.getExtenderClassName()).isEqualTo(ExtenderAlternativeImpl.class.getName());
    }




}
