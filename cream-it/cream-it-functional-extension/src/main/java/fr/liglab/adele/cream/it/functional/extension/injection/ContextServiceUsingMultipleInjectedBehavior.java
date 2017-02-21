package fr.liglab.adele.cream.it.functional.extension.injection;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.functional.extension.InjectedFunctionalExtension;

@ContextEntity(services = ServiceContextPrime.class)
@FunctionalExtension(id="injectedBehavior", contextServices = ExtensionToInject.class,implementation = InjectedExtensionImpl.class)
@FunctionalExtension(id="injectedBehavior2", contextServices = ExtensionToInjectPrime.class,implementation = InjectedExtensionPrimeImpl.class)
public class ContextServiceUsingMultipleInjectedBehavior implements ServiceContextPrime {

    @InjectedFunctionalExtension(id="injectedBehavior")
    ExtensionToInject extensionToInjected;

    @InjectedFunctionalExtension(id="injectedBehavior2")
    ExtensionToInjectPrime behaviorToInjectedPrime;

    @Override
    public boolean returnTrueFromAnInjectedBehavior() {
        return extensionToInjected.getTrue();
    }

    @Override
    public boolean returnFalseFromAnInjectedBehavior() {
        return behaviorToInjectedPrime.getFalse();
    }
}
