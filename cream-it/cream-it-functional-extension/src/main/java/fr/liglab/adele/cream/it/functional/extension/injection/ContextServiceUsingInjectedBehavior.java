package fr.liglab.adele.cream.it.functional.extension.injection;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.functional.extension.InjectedFunctionalExtension;

@ContextEntity(coreServices = ServiceContext.class)
@FunctionalExtension(id = "injectedBehavior", contextServices = ExtensionToInject.class, implementation = InjectedExtensionImpl.class)
public class ContextServiceUsingInjectedBehavior implements ServiceContext {

    @InjectedFunctionalExtension(id = "injectedBehavior")
    ExtensionToInject extensionToInjected;

    @Override
    public boolean returnTrueFromTheInjectedBehavior() {
        return extensionToInjected.getTrue();
    }
}
