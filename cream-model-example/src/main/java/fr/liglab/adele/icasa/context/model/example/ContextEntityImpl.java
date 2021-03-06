package fr.liglab.adele.icasa.context.model.example;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.entity.ContextEntity.State;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;


@ContextEntity(coreServices = ContextEntityDescription.class)
@FunctionalExtension(id = "localisableBehavior", contextServices = BehaviorService.class, implementation = BehaviorImpl.class)
@FunctionalExtension(id = "behaviorBis", contextServices = BehaviorServiceBis.class, implementation = BehaviorImplBis.class)
public class ContextEntityImpl implements ContextEntityDescription {

    public String serial;
    private String externalValue = "external value";
    private int accessCount = 0;
    @State.Field(service = ContextEntityDescription.class, state = ContextEntityDescription.HELLO, directAccess = false)
    private String hello;
    @State.Pull(service = ContextEntityDescription.class, state = ContextEntityDescription.HELLO, period = 3, unit = TimeUnit.SECONDS)
    private Supplier<String> pull = () -> {
        /*
		 * TODO If a value was pushed, we continue to return the last value kept by hand inside this class.
		 * A pull should have a way to signal the context framework to use its internally cached value.
		 */
        return externalValue + "[" + (accessCount++) + "]";
    };
    @State.Apply(service = ContextEntityDescription.class, state = ContextEntityDescription.HELLO)
    private Consumer<String> apply = (value) -> {
        if (!isLastPulledValue(value)) {
            this.externalValue = value;
            this.accessCount = 0;
        }
    };

    private final boolean isLastPulledValue(String value) {
        return (externalValue == null && value == null) ||
                (externalValue != null && value != null && value.startsWith(externalValue));
    }

    @State.Push(service = ContextEntityDescription.class, state = ContextEntityDescription.HELLO)
    public String externalNotification(String externalEvent) {
        String pushedValue = getValue(externalEvent);
        if (!isLastPulledValue(pushedValue)) {
            this.externalValue = pushedValue;
            this.accessCount = 0;
        }
        return pushedValue;
    }

    private String getValue(String event) {
        return event;
    }

    @Override
    public String hello() {
        return hello;
    }

    @Override
    public void setHello(String hello) {
        this.hello = hello;
    }

    @Override
    public String getSerialNumber() {
        return serial;
    }


}