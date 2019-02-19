package fr.liglab.adele.cream.it.functional.extension.test.contextSource;

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
import fr.liglab.adele.cream.it.functional.extension.contextSource.ContextEntityImpl;
import fr.liglab.adele.cream.it.functional.extension.contextSource.ExtensionSpec1;
import fr.liglab.adele.cream.it.functional.extension.test.FunctionalExtensionBaseCommonConfig;
import fr.liglab.adele.cream.testing.helpers.FunctionalExtensionHelper;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.handlers.dependency.DependencyDescription;
import org.apache.felix.ipojo.handlers.dependency.DependencyHandlerDescription;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import static org.fest.assertions.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class FunctionalExtensionContextSourceTest extends FunctionalExtensionBaseCommonConfig {

    @Test
    public void testBehaviorAsIPOJOsource() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ComponentInstance instance = createEntityWithBehaviorChangeOn();
        ExtensionSpec1 serviceObj1 = osgiHelper.waitForService(ExtensionSpec1.class, null, ((long) 2000));
        assertThat(getFilter(instance,"context")).isEqualTo("(state=${" + ContextEntity.State.id(ExtensionSpec1.class, ExtensionSpec1.EXTENSION_STATE) + "})");

        serviceObj1.setValue("value1");
        assertThat(getFilter(instance,"context")).isEqualTo("(state=value1)");

        serviceObj1.setValue("value2");
        assertThat(getFilter(instance,"context")).isEqualTo("(state=value2)");


        FunctionalExtensionHelper functionalExtensionHelper = contextHelper.getFunctionalExtensionHelper();
        Assertions.assertThat(functionalExtensionHelper.getFunctionalExtension(instance, "behavior1")).isNotNull();
        functionalExtensionHelper.stopFunctionalExtension(instance, "behavior1");

        assertThat(getFilter(instance,"context")).isEqualTo("(state=${" + ContextEntity.State.id(ExtensionSpec1.class, ExtensionSpec1.EXTENSION_STATE) + "})");
    }


    private ComponentInstance createEntityWithBehaviorChangeOn() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        return contextHelper.getContextEntityHelper().createContextEntity(ContextEntityImpl.class.getName(), "ContextEntityTest", null);
    }


    private String getFilter(ComponentInstance instance, String id) {
        
    	DependencyHandlerDescription requires = (DependencyHandlerDescription) instance.getInstanceDescription().getHandlerDescription(HandlerFactory.IPOJO_NAMESPACE + ":requires");
        for (DependencyDescription dependency : requires.getDependencies()) {
        	if (id.equals(dependency.getId())) {
        		return dependency.getFilter();
        	}
		}

        return null;
    }
}
