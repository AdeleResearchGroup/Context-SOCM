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


import fr.liglab.adele.cream.it.behavior.synchronisation.BehaviorSpec1;
import fr.liglab.adele.cream.it.behavior.synchronisation.ContextEntity1;
import fr.liglab.adele.cream.it.behavior.synchronisation.ContextService1;
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
public class BehaviorDeparture extends BehaviorBaseCommonConfig {

    @Test
    public void testBehaviorDeparture() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createContextEntity();

        ContextService1 serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);
        assertThat(serviceObj1).isNotNull();

        BehaviorSpec1 behavior = osgiHelper.waitForService(BehaviorSpec1.class,null,((long)2000));
        assertThat(behavior).isNotNull();

        BehaviorHelper behaviorHelper = contextHelper.getBehaviorHelper();
        assertThat(behaviorHelper.getBehavior(instance,"Behavior1")).isNotNull();

        behaviorHelper.stopBehavior(instance,"Behavior1");

        BehaviorSpec1 behavior2 = osgiHelper.getServiceObject(BehaviorSpec1.class);
        assertThat(behavior2).isNull();


    }

    private ComponentInstance createContextEntity() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextEntity1.class.getName(),"ContextEntityTest",null);
    }

}
