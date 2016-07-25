package fr.liglab.adele.cream.it.test;

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


import fr.adele.cream.testing.helpers.ContextBaseTest;
import fr.liglab.adele.cream.it.behavior.ContextEntity1;
import org.apache.felix.ipojo.Factory;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.InvalidSyntaxException;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExamReactorStrategy(PerMethod.class)
public class ContextTest extends ContextBaseTest {

    @Override
    protected List<String> getExtraExports() {
        return Arrays.asList(
                "fr.liglab.adele.cream.it.behavior"
        );
    }


    @Override
    public boolean deployTestBundle() {
        return true;
    }


    @Test
    public void testContextEntityFactoIsPresent() throws InvalidSyntaxException {
        Factory contextFacto = contextHelper.getContextEntityHelper().getContextEntityFactory(ContextEntity1.class.getName());
        System.out.println(contextFacto.getBundleContext().getBundle());
        assertThat(contextFacto).isNotNull();
    }
}
