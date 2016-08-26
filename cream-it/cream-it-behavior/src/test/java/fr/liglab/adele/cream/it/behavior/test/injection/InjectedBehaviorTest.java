package fr.liglab.adele.cream.it.behavior.test.injection;

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


import fr.liglab.adele.cream.it.behavior.injection.ContextServiceUsingInjectedBehavior;
import fr.liglab.adele.cream.it.behavior.injection.ContextServiceUsingMultipleInjectedBehavior;
import fr.liglab.adele.cream.it.behavior.injection.ServiceContext;
import fr.liglab.adele.cream.it.behavior.injection.ServiceContextPrime;
import fr.liglab.adele.cream.it.behavior.test.BehaviorBaseCommonConfig;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import static org.assertj.core.api.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class InjectedBehaviorTest extends BehaviorBaseCommonConfig {

    @Test
    public void testSimpleBehaviorInjection() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntity();

        ServiceContext serviceObj1 = osgiHelper.getServiceObject(ServiceContext.class);

        assertThat(serviceObj1).isNotNull();
        assertThat(serviceObj1.returnTrueFromTheInjectedBehavior()).isTrue();
    }



    @Test
    public void testMultipleBehaviorInjection() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createMultipleContextEntityInjectedBehavior();

        ServiceContextPrime serviceObj1 = osgiHelper.getServiceObject(ServiceContextPrime.class);

        assertThat(serviceObj1).isNotNull();
        assertThat(serviceObj1.returnTrueFromAnInjectedBehavior()).isTrue();

        assertThat(serviceObj1.returnFalseFromAnInjectedBehavior()).isFalse();
    }


    private void createContextEntity() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        contextHelper.getContextEntityHelper().createContextEntity(ContextServiceUsingInjectedBehavior.class.getName(),"ContextServiceUsingInjectedBehavior",null);
    }

    private void createMultipleContextEntityInjectedBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        contextHelper.getContextEntityHelper().createContextEntity(ContextServiceUsingMultipleInjectedBehavior.class.getName(),"ContextServiceUsingMultipleInjectedBehavior",null);
    }
}
