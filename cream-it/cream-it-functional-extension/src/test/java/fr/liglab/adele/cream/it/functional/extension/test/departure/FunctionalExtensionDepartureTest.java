package fr.liglab.adele.cream.it.functional.extension.test.departure;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Core [IntegrationTests]
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.it.functional.extension.injection.ContextServiceUsingInjectedBehavior;
import fr.liglab.adele.cream.it.functional.extension.injection.ExtensionToInject;
import fr.liglab.adele.cream.it.functional.extension.injection.ServiceContext;
import fr.liglab.adele.cream.it.functional.extension.synchronisation.*;
import fr.liglab.adele.cream.it.functional.extension.test.FunctionalExtensionBaseCommonConfig;
import fr.liglab.adele.cream.testing.helpers.FunctionalExtensionHelper;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.ServiceReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class FunctionalExtensionDepartureTest extends FunctionalExtensionBaseCommonConfig {

    @Test
    public void testSimpleBehaviorDeparture() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createContextEntityWithOneBehavior();

        ContextService1 serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);
        assertThat(serviceObj1).isNotNull();

        ExtensionSpec1 behavior = osgiHelper.waitForService(ExtensionSpec1.class, null, ((long) 2000));
        assertThat(behavior).isNotNull();

        FunctionalExtensionHelper functionalExtensionHelper = contextHelper.getFunctionalExtensionHelper();
        assertThat(functionalExtensionHelper.getBehavior(instance, "Behavior1")).isNotNull();

        functionalExtensionHelper.stopBehavior(instance, "Behavior1");

        ExtensionSpec1 behavior2 = osgiHelper.getServiceObject(ExtensionSpec1.class);
        assertThat(behavior2).isNull();

        ContextService1 serviceObj1SideEffect = osgiHelper.getServiceObject(ContextService1.class);
        assertThat(serviceObj1SideEffect).isNotNull();

    }

    @Test
    public void testMultipleBehaviorDeparture() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createContextEntityWithMultipleBehavior();

        ContextService1 serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);
        assertThat(serviceObj1).isNotNull();

        ExtensionSpec1 behavior = osgiHelper.waitForService(ExtensionSpec1.class, null, ((long) 2000));
        FunctionalExtension2 behavior2 = osgiHelper.waitForService(FunctionalExtension2.class, null, ((long) 2000));

        ServiceReference serviceReference = osgiHelper.getServiceReference(ContextService1.class);

        assertThat(behavior).isNotNull();
        assertThat(behavior2).isNotNull();

        FunctionalExtensionHelper functionalExtensionHelper = contextHelper.getFunctionalExtensionHelper();
        assertThat(functionalExtensionHelper.getBehavior(instance, "Behavior1")).isNotNull();
        assertThat(functionalExtensionHelper.getBehavior(instance, "Behavior2")).isNotNull();
        assertThat(serviceReference.getProperty(ContextEntity.State.id(ExtensionSpec1.class, ExtensionSpec1.PARAM_1_DIRECTACCESS))).isEqualTo(ExtensionSpec1.PARAM_1_INIT_VALUE);

        behavior = null;
        behavior2 = null;

        functionalExtensionHelper.invalidBehavior(instance, "Behavior1");
        ExtensionSpec1 extensionSpec1Null = osgiHelper.getServiceObject(ExtensionSpec1.class);
        FunctionalExtension2 functionalExtension2NotNullSideEffect = osgiHelper.getServiceObject(FunctionalExtension2.class);
        assertThat(extensionSpec1Null).isNull();
        assertThat(functionalExtension2NotNullSideEffect).isNotNull();

        /**
         * Property Check
         */
        serviceReference = osgiHelper.getServiceReference(ContextService1.class);
        assertThat(serviceReference.getProperty(ContextEntity.State.id(ExtensionSpec1.class, ExtensionSpec1.PARAM_1_DIRECTACCESS))).isEqualTo(null);


        functionalExtension2NotNullSideEffect = null;

        functionalExtensionHelper.invalidBehavior(instance, "Behavior2");
        FunctionalExtension2 functionalExtension2Null = osgiHelper.getServiceObject(FunctionalExtension2.class);
        assertThat(functionalExtension2Null).isNull();

        /**
         * Property Check
         */
        serviceReference = osgiHelper.getServiceReference(ContextService1.class);
        assertThat(serviceReference.getProperty(ContextEntity.State.id(ExtensionSpec1.class, ExtensionSpec1.PARAM_1_DIRECTACCESS))).isEqualTo(null);


        ExtensionSpec1 extensionSpec1NullCheckSideEffect = osgiHelper.getServiceObject(ExtensionSpec1.class);
        assertThat(extensionSpec1NullCheckSideEffect).isNull();

        /**
         * Restart behavior 1
         */
        functionalExtensionHelper.validBehavior(instance, "Behavior1");
        serviceReference = osgiHelper.getServiceReference(ContextService1.class);
        /**
         * Property Check
         */
        assertThat(serviceReference.getProperty(ContextEntity.State.id(ExtensionSpec1.class, ExtensionSpec1.PARAM_1_DIRECTACCESS))).isEqualTo(ExtensionSpec1.PARAM_1_INIT_VALUE);
    }

    @Test
    public void testProxyExposedInterface() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createContextEntityWithMultipleBehavior();

        ContextService1 serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);
        assertThat(serviceObj1).isNotNull();

        ExtensionSpec1 behavior = osgiHelper.waitForService(ExtensionSpec1.class, null, ((long) 2000));
        FunctionalExtension2 behavior2 = osgiHelper.waitForService(FunctionalExtension2.class, null, ((long) 2000));
        assertThat(behavior).isNotNull();
        assertThat(behavior2).isNotNull();

        assertThat(behavior).isInstanceOf(ContextService1.class);
        assertThat(behavior2).isInstanceOf(ContextService1.class);

        FunctionalExtensionHelper functionalExtensionHelper = contextHelper.getFunctionalExtensionHelper();
        assertThat(functionalExtensionHelper.getBehavior(instance, "Behavior1")).isNotNull();
        assertThat(functionalExtensionHelper.getBehavior(instance, "Behavior2")).isNotNull();

        behavior = null;
        behavior2 = null;

        functionalExtensionHelper.stopBehavior(instance, "Behavior1");
        ExtensionSpec1 extensionSpec1Null = osgiHelper.getServiceObject(ExtensionSpec1.class);
        FunctionalExtension2 functionalExtension2NotNullSideEffect = osgiHelper.getServiceObject(FunctionalExtension2.class);
        ContextService1 contextServiceProxyAfterBehavior1Departure = osgiHelper.getServiceObject(ContextService1.class);

        assertThat(extensionSpec1Null).isNull();
        assertThat(functionalExtension2NotNullSideEffect).isNotNull();

        assertThat(contextServiceProxyAfterBehavior1Departure).isInstanceOf(FunctionalExtension2.class);
        assertThat(contextServiceProxyAfterBehavior1Departure).isNotInstanceOf(ExtensionSpec1.class);

        functionalExtension2NotNullSideEffect = null;
        contextServiceProxyAfterBehavior1Departure = null;
        extensionSpec1Null = null;

        functionalExtensionHelper.stopBehavior(instance, "Behavior2");
        FunctionalExtension2 functionalExtension2Null = osgiHelper.getServiceObject(FunctionalExtension2.class);
        ContextService1 contextServiceProxyAfterBehavior2Departure = osgiHelper.getServiceObject(ContextService1.class);

        assertThat(functionalExtension2Null).isNull();

        assertThat(contextServiceProxyAfterBehavior2Departure).isNotInstanceOf(FunctionalExtension2.class);
        assertThat(contextServiceProxyAfterBehavior2Departure).isNotInstanceOf(ExtensionSpec1.class);

        ExtensionSpec1 extensionSpec1NullCheckSideEffect = osgiHelper.getServiceObject(ExtensionSpec1.class);
        assertThat(extensionSpec1NullCheckSideEffect).isNull();

    }

    @Test
    public void testDepartureWithBehaviorInjection() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createContextEntityInjectedBehavior();

        ServiceContext serviceObj1 = osgiHelper.waitForService(ServiceContext.class, null, ((long) 2000));
        assertThat(serviceObj1).isNotNull();

        ExtensionToInject extensionToInject = osgiHelper.waitForService(ExtensionToInject.class, null, ((long) 2000));
        assertThat(extensionToInject).isNotNull();

        FunctionalExtensionHelper functionalExtensionHelper = contextHelper.getFunctionalExtensionHelper();
        assertThat(functionalExtensionHelper.getBehavior(instance, "injectedBehavior")).isNotNull();

        extensionToInject = null;
        serviceObj1 = null;

        functionalExtensionHelper.stopBehavior(instance, "injectedBehavior");

        serviceObj1 = osgiHelper.getServiceObject(ServiceContext.class);
        extensionToInject = osgiHelper.getServiceObject(ExtensionToInject.class);

        assertThat(extensionToInject).isNull();
        assertThat(serviceObj1).isNull();
    }

    private ComponentInstance createContextEntityInjectedBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextServiceUsingInjectedBehavior.class.getName(), "ContextServiceUsingInjectedBehavior", null);
    }

    private ComponentInstance createContextEntityWithOneBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextEntity1.class.getName(), "ContextEntityTest", null);
    }


    private ComponentInstance createContextEntityWithMultipleBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextEntity2.class.getName(), "ContextEntityTest", null);
    }

}
