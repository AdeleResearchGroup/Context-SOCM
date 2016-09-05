package fr.liglab.adele.cream.it.behavior.test.synchronisation;

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
import fr.liglab.adele.cream.it.behavior.synchronisation.BehaviorSpec2;
import fr.liglab.adele.cream.it.behavior.synchronisation.ContextEntity2;
import fr.liglab.adele.cream.it.behavior.synchronisation.ContextService1;
import fr.liglab.adele.cream.it.behavior.test.BehaviorBaseCommonConfig;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import static org.assertj.core.api.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class MultipleBehaviorTest extends BehaviorBaseCommonConfig {

    @Test
    public void testMultipleBehaviorServiceExposition() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntity();

        Object serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);

        assertThat(serviceObj1).isNotNull();

        Object serviceObj2 = osgiHelper.waitForService(BehaviorSpec1.class,null,((long)0),true);
        Object serviceObj3 = osgiHelper.waitForService(BehaviorSpec2.class,null,((long)0),true);



        assertThat(serviceObj1 instanceof ContextService1).isTrue();
        assertThat(serviceObj1 instanceof BehaviorSpec1).isTrue();
        assertThat(serviceObj1 instanceof BehaviorSpec2).isTrue();

        assertThat(serviceObj2 instanceof ContextService1).isTrue();
        assertThat(serviceObj2 instanceof BehaviorSpec1).isTrue();
        assertThat(serviceObj2 instanceof BehaviorSpec2).isTrue();

        assertThat(serviceObj3 instanceof ContextService1).isTrue();
        assertThat(serviceObj3 instanceof BehaviorSpec1).isTrue();
        assertThat(serviceObj3 instanceof BehaviorSpec2).isTrue();


    }

    private void createContextEntity() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        contextHelper.getContextEntityHelper().createContextEntity(ContextEntity2.class.getName(),"ContextEntityTest",null);
    }
}
