package fr.liglab.adele.cream.testing.helpers;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Testing Helpers
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

import org.junit.Before;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.util.PathUtils;
import org.ow2.chameleon.testing.helpers.BaseTest;

import java.io.IOException;

import static org.ops4j.pax.exam.CoreOptions.*;

public abstract class ContextBaseTest extends BaseTest {

    protected ContextHelper contextHelper;

    public Option[] defaultConfiguration() throws IOException {
        Option[] options = super.defaultConfiguration();
        options = OptionUtils.combine(options, wisdomBundle());
        options = OptionUtils.combine(options, creamBundles());
        options = OptionUtils.combine(options, assertjBundles());
        options = OptionUtils.combine(options, festBundles());
        options = OptionUtils.combine(options,  systemProperty("logback.configurationFile")
                .value("file:" + PathUtils.getBaseDir() + "/src/test/resources/logger.xml"));
        options = OptionUtils.combine(options,   log());

        if (deployCreamRuntimeFacilities()){
            options = OptionUtils.combine(options, creamRuntimeFacilitiesBundles());
        }

        return options;
    }


    protected CompositeOption log() {
        return new DefaultCompositeOption(
                mavenBundle("org.apache.felix", "org.apache.felix.log").versionAsInProject(),
                mavenBundle("org.slf4j", "slf4j-api").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-core").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject()
        );
    }

    protected Option creamBundles() {
        return composite(
                mavenBundle().groupId("fr.liglab.adele.cream").artifactId("cream.model.runtime").versionAsInProject(),
                mavenBundle().groupId("fr.liglab.adele.cream").artifactId("cream.core").versionAsInProject(),
                mavenBundle().groupId("fr.liglab.adele.cream").artifactId("cream.helpers").versionAsInProject()
        );
    }

    protected Option creamRuntimeFacilitiesBundles() {
        return composite(
                mavenBundle().groupId("fr.liglab.adele.cream").artifactId("cream.runtime.facilities").versionAsInProject()
        );
    }

    protected Option assertjBundles() {
        return composite(
                wrappedBundle(mavenBundle("org.assertj", "assertj-core").versionAsInProject())
        );
    }

    protected CompositeOption festBundles() {
        return new DefaultCompositeOption(
                wrappedBundle(CoreOptions.mavenBundle("org.easytesting", "fest-util").versionAsInProject()),
                wrappedBundle(CoreOptions.mavenBundle("org.easytesting", "fest-assert").versionAsInProject())
        );
    }

    protected Option wisdomBundle() {
        return composite(
                mavenBundle().groupId("org.wisdom-framework").artifactId("wisdom-executors").versionAsInProject(),
                mavenBundle().groupId("fr.liglab.adele.cream").artifactId("wisdom.fake.bundle").versionAsInProject(),
                mavenBundle().groupId("org.wisdom-framework").artifactId("wisdom-api").versionAsInProject(),
                mavenBundle().groupId("commons-io").artifactId("commons-io").versionAsInProject(),
                mavenBundle().groupId("com.google.guava").artifactId("guava").versionAsInProject(),
                mavenBundle().groupId("joda-time").artifactId("joda-time").versionAsInProject(),
                mavenBundle().groupId("com.fasterxml.jackson.core").artifactId("jackson-databind").versionAsInProject(),
                mavenBundle().groupId("com.fasterxml.jackson.core").artifactId("jackson-core").versionAsInProject(),
                mavenBundle().groupId("com.fasterxml.jackson.core").artifactId("jackson-annotations").versionAsInProject(),
                mavenBundle().groupId("com.fasterxml.jackson.dataformat").artifactId("jackson-dataformat-xml").versionAsInProject(),
                mavenBundle().groupId("com.fasterxml.jackson.module").artifactId("jackson-module-jaxb-annotations").versionAsInProject(),
                mavenBundle().groupId("javax.validation").artifactId("validation-api").versionAsInProject(),
                mavenBundle().groupId("org.codehaus.woodstox").artifactId("stax2-api").versionAsInProject(),
                mavenBundle().groupId("org.codehaus.woodstox").artifactId("woodstox-core-asl").versionAsInProject()


        );
    }

    @Override
    public final boolean deployiPOJO() {
        return true;
    }


    @Override
    public final boolean deployMockito() {
        return true;
    }

    public boolean deployCreamRuntimeFacilities() {
        return false;
    }

    /**
     * Done some initializations.
     */
    @Before
    public void fuchsiaSetUp() {
        contextHelper = new ContextHelper(this.osgiHelper,this.ipojoHelper);
    }

}
