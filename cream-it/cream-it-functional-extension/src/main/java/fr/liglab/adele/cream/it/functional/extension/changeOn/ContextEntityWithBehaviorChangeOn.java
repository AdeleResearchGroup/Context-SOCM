package fr.liglab.adele.cream.it.functional.extension.changeOn;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

@ContextEntity(coreServices = ServiceOfContext.class)
@FunctionalExtension(contextServices = ExtensionSpec.class, implementation = ExtenderImpl.class, id = "changOnBehavior")
public class ContextEntityWithBehaviorChangeOn implements ServiceOfContext {

    @ContextEntity.State.Field(service = ServiceOfContext.class, state = ServiceOfContext.STATE_1, directAccess = true)
    private boolean state1;

    @Override
    public void setState1(boolean newState) {
        state1 = newState;
    }
}
