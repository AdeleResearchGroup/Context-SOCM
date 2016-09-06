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
        assertThat(behavior).isNotNull();
        assertThat(behavior2).isNotNull();

        BehaviorHelper behaviorHelper = contextHelper.getBehaviorHelper();
        assertThat(behaviorHelper.getBehavior(instance,"Behavior1")).isNotNull();
        assertThat(behaviorHelper.getBehavior(instance,"Behavior2")).isNotNull();

        behavior = null;
        behavior2 = null;

        behaviorHelper.stopBehavior(instance,"Behavior1");
        BehaviorSpec1 behaviorSpec1Null = osgiHelper.getServiceObject(BehaviorSpec1.class);
        BehaviorSpec2 behaviorSpec2NotNullSideEffect = osgiHelper.getServiceObject(BehaviorSpec2.class);
        assertThat(behaviorSpec1Null).isNull();
        assertThat(behaviorSpec2NotNullSideEffect).isNotNull();

        behaviorSpec2NotNullSideEffect = null;

        behaviorHelper.stopBehavior(instance,"Behavior2");
        BehaviorSpec2 behaviorSpec2Null = osgiHelper.getServiceObject(BehaviorSpec2.class);
        assertThat(behaviorSpec2Null).isNull();

        BehaviorSpec1 behaviorSpec1NullCheckSideEffect = osgiHelper.getServiceObject(BehaviorSpec1.class);
        assertThat(behaviorSpec1NullCheckSideEffect).isNull();

    }

    private ComponentInstance createContextEntityWithOneBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextEntity1.class.getName(),"ContextEntityTest",null);
    }


    private ComponentInstance createContextEntityWithMultipleBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextEntity2.class.getName(),"ContextEntityTest",null);
    }

}
