package fr.liglab.adele.cream.it.entity.test.services;

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


import fr.liglab.adele.cream.it.entity.services.*;
import fr.liglab.adele.cream.it.entity.test.EntityBaseCommonConfig;
import org.apache.felix.ipojo.*;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import static org.assertj.core.api.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class EntityServiceTest extends EntityBaseCommonConfig {


    @Test
    public void testSimpleContextService() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntity();
        ContextService1 serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);

        assertThat(serviceObj1).isNotNull();
        assertThat(serviceObj1.returnFalse()).isFalse();
    }
    @Test
    public void testContextExplicitServiceHeritage() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntityExplicitHeritage();
        ContextServiceHeritage serviceObj1 = osgiHelper.getServiceObject(ContextServiceHeritage.class);

        assertThat(serviceObj1).isNotNull();
        assertThat(serviceObj1.returnTrue()).isTrue();
        assertThat(serviceObj1.returnFalse()).isFalse();
    }

    @Test
    public void testContextImplicitServiceHeritage() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntityHeritage();
        ContextServiceHeritage serviceObj1 = osgiHelper.getServiceObject(ContextServiceHeritage.class);

        assertThat(serviceObj1).isNotNull();
        assertThat(serviceObj1.returnTrue()).isTrue();
        assertThat(serviceObj1.returnFalse()).isFalse();
    }

    @Test
    public void tesPojoInterfaceInvocation() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntity();

        Object serviceObj1 = osgiHelper.getServiceObject(ContextService1.class);

        assertThat(serviceObj1 instanceof ContextService1).isTrue();
        assertThat(serviceObj1 instanceof Pojo).isTrue();

        Pojo pojo = (Pojo)serviceObj1;

        ComponentInstance instance = pojo.getComponentInstance();

        assertThat(instance).isNotNull();

    }

    private void createContextEntity() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        contextHelper.getContextEntityHelper().createContextEntity(ContextEntity1.class.getName(),"ContextEntityTest",null);
    }

    private void createContextEntityHeritage() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        contextHelper.getContextEntityHelper().createContextEntity(ContextEntityHeritageImpl.class.getName(),"ContextEntityHeritageTest",null);
    }

    private void createContextEntityExplicitHeritage() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        contextHelper.getContextEntityHelper().createContextEntity(ContextEntityExplicitHeritageImpl.class.getName(),"ContextEntityExplicitHeritageTest",null);
    }
}
