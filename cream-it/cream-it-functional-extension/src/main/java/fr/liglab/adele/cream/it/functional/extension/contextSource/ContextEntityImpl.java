package fr.liglab.adele.cream.it.functional.extension.contextSource;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import org.apache.felix.ipojo.annotations.Requires;

@ContextEntity(services = {})

@FunctionalExtension(id="behavior1",implementation = FunctionalExtenderImpl1.class, contextServices = ExtensionSpec1.class)
public class ContextEntityImpl {

    @Requires(filter = "(state=${extensionspec1.state})",optional = true)
    FakeService service;

}
