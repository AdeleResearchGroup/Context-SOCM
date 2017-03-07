package fr.liglab.adele.cream.it.functional.extension.test.changeOn;

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


import fr.liglab.adele.cream.it.functional.extension.changeOn.ContextEntityWithBehaviorChangeOn;
import fr.liglab.adele.cream.it.functional.extension.changeOn.ExtensionSpec;
import fr.liglab.adele.cream.it.functional.extension.changeOn.ServiceOfContext;
import fr.liglab.adele.cream.it.functional.extension.test.FunctionalExtensionBaseCommonConfig;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import static org.assertj.core.api.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class FunctionalExtensionChangeOnTest extends FunctionalExtensionBaseCommonConfig {

    @Test
    public void testChangeOnCall() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createEntityWithBehaviorChangeOn();

        ServiceOfContext serviceObj1 = osgiHelper.getServiceObject(ServiceOfContext.class);
        assertThat(serviceObj1).isNotNull();

        ExtensionSpec behavior = osgiHelper.waitForService(ExtensionSpec.class, null, ((long) 2000));
        assertThat(behavior).isNotNull();

        serviceObj1.setState1(false);

        assertThat(behavior.getChange()).isFalse();


        serviceObj1.setState1(true);

        assertThat(behavior.getChange()).isTrue();

    }


    private ComponentInstance createEntityWithBehaviorChangeOn() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextEntityWithBehaviorChangeOn.class.getName(), "ContextEntityTest", null);
    }

}
