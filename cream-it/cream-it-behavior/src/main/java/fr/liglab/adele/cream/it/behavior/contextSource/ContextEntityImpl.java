package fr.liglab.adele.cream.it.behavior.contextSource;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import org.apache.felix.ipojo.annotations.Requires;

@ContextEntity(services = {})

@Behavior(id="behavior1",implem = BehaviorImpl1.class, spec = BehaviorService1.class)
public class ContextEntityImpl {

    @Requires(filter = "(state=${behaviorservice1.state})",optional = true)
    FakeService service;

}
