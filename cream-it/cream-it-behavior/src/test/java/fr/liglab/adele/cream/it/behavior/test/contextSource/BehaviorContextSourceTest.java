package fr.liglab.adele.cream.it.behavior.test.contextSource;

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
import fr.liglab.adele.cream.it.behavior.contextSource.BehaviorService1;
import fr.liglab.adele.cream.it.behavior.contextSource.ContextEntityImpl;
import fr.liglab.adele.cream.it.behavior.test.BehaviorBaseCommonConfig;
import fr.liglab.adele.cream.testing.helpers.BehaviorHelper;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import static org.fest.assertions.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class BehaviorContextSourceTest extends BehaviorBaseCommonConfig {

    @Test
    public void testBehaviorAsIPOJOsource() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createEntityWithBehaviorChangeOn();
        BehaviorService1 serviceObj1 = osgiHelper.waitForService(BehaviorService1.class, null, ((long) 2000));
        assertThat(extractStateFilter(instance)).isEqualTo("${"+ ContextEntity.State.id(BehaviorService1.class, BehaviorService1.BEHAVIOR_STATE)+"}");

        serviceObj1.setValue("value1");
        assertThat(extractStateFilter(instance)).isEqualTo("value1");

        serviceObj1.setValue("value2");
        assertThat(extractStateFilter(instance)).isEqualTo("value2");


        BehaviorHelper behaviorHelper = contextHelper.getBehaviorHelper();
        Assertions.assertThat(behaviorHelper.getBehavior(instance,"behavior1")).isNotNull();
        behaviorHelper.stopBehavior(instance,"behavior1");

        assertThat(extractStateFilter(instance)).isEqualTo("${"+ ContextEntity.State.id(BehaviorService1.class, BehaviorService1.BEHAVIOR_STATE)+"}");
    }



    private ComponentInstance createEntityWithBehaviorChangeOn() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextEntityImpl.class.getName(),"ContextEntityTest",null);
    }


    private String extractStateFilter(ComponentInstance instance){
        HandlerDescription description = instance.getInstanceDescription().getHandlerDescription(HandlerFactory.IPOJO_NAMESPACE+":requires");
        Element element = description.getHandlerInfo();

        for (Element requiresElement : element.getElements()){
            String filter = requiresElement.getAttribute("filter");
            if (filter != null){
                return filter.substring(7,filter.length()-1);
            }
        }
        return null;
    }
}
