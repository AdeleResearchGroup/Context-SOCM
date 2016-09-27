package fr.liglab.adele.cream.it.behavior.test.departure;

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
import fr.liglab.adele.cream.it.behavior.injection.BehaviorToInject;
import fr.liglab.adele.cream.it.behavior.injection.ContextServiceUsingInjectedBehavior;
import fr.liglab.adele.cream.it.behavior.injection.ServiceContext;
import fr.liglab.adele.cream.it.behavior.synchronisation.*;
import fr.liglab.adele.cream.it.behavior.test.BehaviorBaseCommonConfig;
import fr.liglab.adele.cream.testing.helpers.BehaviorHelper;
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
public class BehaviorDepartureTest extends BehaviorBaseCommonConfig {

    @Test
    public void testSimpleBehaviorDeparture() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createContextEntityWithOneBehavior();

        ContextService1 serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);
        assertThat(serviceObj1).isNotNull();

        BehaviorSpec1 behavior = osgiHelper.waitForService(BehaviorSpec1.class,null,((long)2000));
        assertThat(behavior).isNotNull();

        BehaviorHelper behaviorHelper = contextHelper.getBehaviorHelper();
        assertThat(behaviorHelper.getBehavior(instance,"Behavior1")).isNotNull();

        behaviorHelper.stopBehavior(instance,"Behavior1");

        BehaviorSpec1 behavior2 = osgiHelper.getServiceObject(BehaviorSpec1.class);
        assertThat(behavior2).isNull();

        ContextService1 serviceObj1SideEffect = osgiHelper.getServiceObject(ContextService1.class);
        assertThat(serviceObj1SideEffect).isNotNull();

    }

    @Test
    public void testMultipleBehaviorDeparture() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createContextEntityWithMultipleBehavior();

        ContextService1 serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);
        assertThat(serviceObj1).isNotNull();

        BehaviorSpec1 behavior = osgiHelper.waitForService(BehaviorSpec1.class,null,((long)2000));
        BehaviorSpec2 behavior2 = osgiHelper.waitForService(BehaviorSpec2.class,null,((long)2000));

        ServiceReference serviceReference = osgiHelper.getServiceReference(ContextService1.class);

        assertThat(behavior).isNotNull();
        assertThat(behavior2).isNotNull();

        BehaviorHelper behaviorHelper = contextHelper.getBehaviorHelper();
        assertThat(behaviorHelper.getBehavior(instance,"Behavior1")).isNotNull();
        assertThat(behaviorHelper.getBehavior(instance,"Behavior2")).isNotNull();
        assertThat(serviceReference.getProperty(ContextEntity.State.id(BehaviorSpec1.class,BehaviorSpec1.PARAM_1_DIRECTACCESS))).isEqualTo(BehaviorSpec1.PARAM_1_INIT_VALUE);

        behavior = null;
        behavior2 = null;

        behaviorHelper.invalidBehavior(instance,"Behavior1");
        BehaviorSpec1 behaviorSpec1Null = osgiHelper.getServiceObject(BehaviorSpec1.class);
        BehaviorSpec2 behaviorSpec2NotNullSideEffect = osgiHelper.getServiceObject(BehaviorSpec2.class);
        assertThat(behaviorSpec1Null).isNull();
        assertThat(behaviorSpec2NotNullSideEffect).isNotNull();

        /**
         * Property Check
         */
        serviceReference = osgiHelper.getServiceReference(ContextService1.class);
        assertThat(serviceReference.getProperty(ContextEntity.State.id(BehaviorSpec1.class,BehaviorSpec1.PARAM_1_DIRECTACCESS))).isEqualTo(null);


        behaviorSpec2NotNullSideEffect = null;

        behaviorHelper.invalidBehavior(instance,"Behavior2");
        BehaviorSpec2 behaviorSpec2Null = osgiHelper.getServiceObject(BehaviorSpec2.class);
        assertThat(behaviorSpec2Null).isNull();

        /**
         * Property Check
         */
        serviceReference = osgiHelper.getServiceReference(ContextService1.class);
        assertThat(serviceReference.getProperty(ContextEntity.State.id(BehaviorSpec1.class,BehaviorSpec1.PARAM_1_DIRECTACCESS))).isEqualTo(null);


        BehaviorSpec1 behaviorSpec1NullCheckSideEffect = osgiHelper.getServiceObject(BehaviorSpec1.class);
        assertThat(behaviorSpec1NullCheckSideEffect).isNull();

        /**
         * Restart behavior 1
         */
        behaviorHelper.validBehavior(instance,"Behavior1");
        serviceReference = osgiHelper.getServiceReference(ContextService1.class);
        /**
         * Property Check
         */
        assertThat(serviceReference.getProperty(ContextEntity.State.id(BehaviorSpec1.class,BehaviorSpec1.PARAM_1_DIRECTACCESS))).isEqualTo(BehaviorSpec1.PARAM_1_INIT_VALUE);
    }

    @Test
    public void testProxyExposedInterface() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createContextEntityWithMultipleBehavior();

        ContextService1 serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);
        assertThat(serviceObj1).isNotNull();

        BehaviorSpec1 behavior = osgiHelper.waitForService(BehaviorSpec1.class,null,((long)2000));
        BehaviorSpec2 behavior2 = osgiHelper.waitForService(BehaviorSpec2.class,null,((long)2000));
        assertThat(behavior).isNotNull();
        assertThat(behavior2).isNotNull();

        assertThat(behavior).isInstanceOf(ContextService1.class);
        assertThat(behavior2).isInstanceOf(ContextService1.class);

        BehaviorHelper behaviorHelper = contextHelper.getBehaviorHelper();
        assertThat(behaviorHelper.getBehavior(instance,"Behavior1")).isNotNull();
        assertThat(behaviorHelper.getBehavior(instance,"Behavior2")).isNotNull();

        behavior = null;
        behavior2 = null;

        behaviorHelper.stopBehavior(instance,"Behavior1");
        BehaviorSpec1 behaviorSpec1Null = osgiHelper.getServiceObject(BehaviorSpec1.class);
        BehaviorSpec2 behaviorSpec2NotNullSideEffect = osgiHelper.getServiceObject(BehaviorSpec2.class);
        ContextService1 contextServiceProxyAfterBehavior1Departure = osgiHelper.getServiceObject(ContextService1.class);

        assertThat(behaviorSpec1Null).isNull();
        assertThat(behaviorSpec2NotNullSideEffect).isNotNull();

        assertThat(contextServiceProxyAfterBehavior1Departure).isInstanceOf(BehaviorSpec2.class);
        assertThat(contextServiceProxyAfterBehavior1Departure).isNotInstanceOf(BehaviorSpec1.class);

        behaviorSpec2NotNullSideEffect = null;
        contextServiceProxyAfterBehavior1Departure = null;
        behaviorSpec1Null = null;

        behaviorHelper.stopBehavior(instance,"Behavior2");
        BehaviorSpec2 behaviorSpec2Null = osgiHelper.getServiceObject(BehaviorSpec2.class);
        ContextService1 contextServiceProxyAfterBehavior2Departure = osgiHelper.getServiceObject(ContextService1.class);

        assertThat(behaviorSpec2Null).isNull();

        assertThat(contextServiceProxyAfterBehavior2Departure).isNotInstanceOf(BehaviorSpec2.class);
        assertThat(contextServiceProxyAfterBehavior2Departure).isNotInstanceOf(BehaviorSpec1.class);

        BehaviorSpec1 behaviorSpec1NullCheckSideEffect = osgiHelper.getServiceObject(BehaviorSpec1.class);
        assertThat(behaviorSpec1NullCheckSideEffect).isNull();

    }

    @Test
    public void testDepartureWithBehaviorInjection() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createContextEntityInjectedBehavior();

        ServiceContext serviceObj1 = osgiHelper.waitForService(ServiceContext.class,null,((long)2000));
        assertThat(serviceObj1).isNotNull();

        BehaviorToInject behaviorToInject = osgiHelper.waitForService(BehaviorToInject.class,null,((long)2000));
        assertThat(behaviorToInject).isNotNull();

        BehaviorHelper behaviorHelper = contextHelper.getBehaviorHelper();
        assertThat(behaviorHelper.getBehavior(instance,"injectedBehavior")).isNotNull();

        behaviorToInject = null;
        serviceObj1 = null;

        behaviorHelper.stopBehavior(instance,"injectedBehavior");

        serviceObj1 = osgiHelper.getServiceObject(ServiceContext.class);
        behaviorToInject = osgiHelper.getServiceObject(BehaviorToInject.class);

        assertThat(behaviorToInject).isNull();
        assertThat(serviceObj1).isNull();
    }

    private ComponentInstance createContextEntityInjectedBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextServiceUsingInjectedBehavior.class.getName(),"ContextServiceUsingInjectedBehavior",null);
    }

    private ComponentInstance createContextEntityWithOneBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextEntity1.class.getName(),"ContextEntityTest",null);
    }


    private ComponentInstance createContextEntityWithMultipleBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextEntity2.class.getName(),"ContextEntityTest",null);
    }

}
