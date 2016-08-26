package fr.liglab.adele.cream.it.entity.test.synchronisation;

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
import fr.liglab.adele.cream.it.entity.synchronisation.ContextEntitySynchro;
import fr.liglab.adele.cream.it.entity.synchronisation.ServiceSynchroSpec;
import fr.liglab.adele.cream.it.entity.test.EntityBaseCommonConfig;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.ServiceReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class EntitySynchroTest extends EntityBaseCommonConfig {


    @Test
    public void testDirectAccessSVBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ServiceSynchroSpec serviceObj1 = createContextEntity();

        ServiceReference serviceReference = osgiHelper.getServiceReference(ServiceSynchroSpec.class);

        assertThat(serviceObj1.getterMethodParam1()).isEqualTo(ServiceSynchroSpec.PARAM_1_INIT_VALUE).overridingErrorMessage("first getter call didn't return the right value");
        assertThat(serviceReference.getProperty(ContextEntity.State.id(ServiceSynchroSpec.class,ServiceSynchroSpec.PARAM_1_DIRECTACCESS))).isEqualTo(ServiceSynchroSpec.PARAM_1_INIT_VALUE).overridingErrorMessage("Service property isn't set to initial value");

        boolean newValue = !ServiceSynchroSpec.PARAM_1_INIT_VALUE;
        serviceObj1.setterMethodParam1(newValue);

        assertThat(serviceObj1.getterMethodParam1()).isEqualTo(newValue);
        assertThat(serviceReference.getProperty(ContextEntity.State.id(ServiceSynchroSpec.class,ServiceSynchroSpec.PARAM_1_DIRECTACCESS))).isEqualTo(newValue).overridingErrorMessage("Service property isn't set to the modified value");

    }

    @Test
    public void testPullSVBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntity();

        ServiceSynchroSpec serviceObj1 = osgiHelper.getServiceObject(ServiceSynchroSpec.class);
        ServiceReference serviceReference = osgiHelper.getServiceReference(ServiceSynchroSpec.class);

        assertThat(serviceObj1.getterMethodParam2()).isEqualTo(ServiceSynchroSpec.PARAM_2_VALUE);
        assertThat(serviceReference.getProperty(ContextEntity.State.id(ServiceSynchroSpec.class,ServiceSynchroSpec.PARAM_2_PULL))).isEqualTo(ServiceSynchroSpec.PARAM_2_VALUE).overridingErrorMessage("Service property isn't set to the pulled value");

        serviceObj1.setterMethodParam2(!ServiceSynchroSpec.PARAM_2_VALUE);

        assertThat(serviceObj1.getterMethodParam2()).isEqualTo(ServiceSynchroSpec.PARAM_2_VALUE);
        assertThat(serviceReference.getProperty(ContextEntity.State.id(ServiceSynchroSpec.class,ServiceSynchroSpec.PARAM_2_PULL))).isEqualTo(ServiceSynchroSpec.PARAM_2_VALUE).overridingErrorMessage("Service property isn't set to the pulled value");

    }

    @Test
    public void testApplySVBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ServiceSynchroSpec serviceObj1 = createContextEntity();

        ServiceReference serviceReference = osgiHelper.getServiceReference(ServiceSynchroSpec.class);

        assertThat( serviceObj1.getterMethodParam3ReturnAlwaysNull()).isEqualTo(null);
        assertThat(serviceObj1.getterMethodParam3WithChange()).isEqualTo(true);
        assertThat(serviceReference.getProperty(ContextEntity.State.id(ServiceSynchroSpec.class,ServiceSynchroSpec.PARAM_3_APPLY))).isEqualTo(null);

        serviceObj1.setterMethodParam3(false);
        assertThat(serviceObj1.getterMethodParam3ReturnAlwaysNull()).isEqualTo(null);
        assertThat(serviceObj1.getterMethodParam3WithChange()).isEqualTo(false);
        assertThat(serviceReference.getProperty(ContextEntity.State.id(ServiceSynchroSpec.class,ServiceSynchroSpec.PARAM_3_APPLY))).isEqualTo(null);


    }

    @Test
    public void testPullApplySVBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ServiceSynchroSpec serviceObj1 = createContextEntity();

        ServiceReference serviceReference = osgiHelper.getServiceReference(ServiceSynchroSpec.class);

        assertThat( serviceObj1.getterMethodParam5()).isEqualTo(true);
        assertThat(serviceObj1.getterMethodParam5WithChange()).isEqualTo(true);

        serviceObj1.setterMethodParam5(false);
        assertThat(serviceObj1.getterMethodParam5WithChange()).isEqualTo(false);

        /**
         * TODO : Test failed due to iPOJO bug, bugfix proposed in FELIX-5314
         *assertThat(serviceObj1.getterMethodParam5()).isEqualTo(true);
         */

        assertThat(serviceObj1.getterMethodParam5WithChange()).isEqualTo(false);


    }

    @Test
    public void testPeriodicPullSVBehavior() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ServiceSynchroSpec serviceObj1 =createContextEntity();

        ServiceReference serviceReference = osgiHelper.getServiceReference(ServiceSynchroSpec.class);
/**
 * TODO : Test fail, check init phase of behavior
 long firstValue = (long) serviceReference.getProperty(ContextEntity.State.id(BehaviorSpec1.class,BehaviorSpec1.PARAM_4_PERIODICPULL));
 **/
        try {
            Thread.currentThread().sleep(2500);
        } catch (InterruptedException e) {
            assertThat(true).isEqualTo(false);
        }

        long secondValue = (long) serviceReference.getProperty(ContextEntity.State.id(ServiceSynchroSpec.class,ServiceSynchroSpec.PARAM_4_PERIODICPULL));

        try {
            Thread.currentThread().sleep(2500);
        } catch (InterruptedException e) {
            assertThat(true).isEqualTo(false);
        }
        long thirdValue = (long) serviceReference.getProperty(ContextEntity.State.id(ServiceSynchroSpec.class,ServiceSynchroSpec.PARAM_4_PERIODICPULL));

        /**
         * see TODO
         assertThat(secondValue).isNotEqualTo(firstValue);
         **/
        assertThat(secondValue).isNotEqualTo(thirdValue);

    }


    private ServiceSynchroSpec createContextEntity() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        contextHelper.getContextEntityHelper().createContextEntity(ContextEntitySynchro.class.getName(),"ContextEntityTest",null);
        return osgiHelper.getServiceObject(ServiceSynchroSpec.class);
    }
}
