package fr.liglab.adele.icasa.context.model.example;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.entity.ContextEntity.State;
import org.apache.felix.ipojo.annotations.Provides;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;


@ContextEntity(services = ContextEntityDescription.class)
@Provides(specifications = BehaviorS.class)
@Behavior(id="localisableBehavior", spec = BehaviorS.class,implem = BehaviorImpl.class)
public class ContextEntityImpl implements ContextEntityDescription {

	private String externalValue = "external value";
	private int accessCount = 0;

	@State.Field(service = ContextEntityDescription.class, state = ContextEntityDescription.HELLO, directAccess = true,value = "Coucou")
	private String hello;

	@State.Pull(service = ContextEntityDescription.class, state = ContextEntityDescription.HELLO)
	private Supplier<String> pull =  () -> {
		/*
		 * TODO If a value was pushed, we continue to return the last value kept by hand inside this class.
		 * A pull should have a way to signal the context framework to use its internally cached value.
		 */
		return externalValue+"["+(accessCount++)+"]";
	};

	@State.Apply(service = ContextEntityDescription.class, state = ContextEntityDescription.HELLO)
	private Consumer<String> apply =  (value) -> {
		if (! isLastPulledValue(value)) {
			this.externalValue 	= value;
			this.accessCount 	= 0;
		}
	};

	private final boolean isLastPulledValue(String value) {
		return 	(externalValue == null && value == null) ||
				(externalValue != null && value != null && value.startsWith(externalValue));
	}

	@State.Push(service = ContextEntityDescription.class, state = ContextEntityDescription.HELLO)
	public String externalNotification(String externalEvent) {
		String pushedValue = getValue(externalEvent);
		if (! isLastPulledValue(pushedValue)) {
			this.externalValue 	= pushedValue;
			this.accessCount 	= 0;
		}
		return pushedValue;
	}

	private String getValue(String event) {
		return event;
	}

	public String serial;

	@Override
	public String hello() {
		return "COUCOU";
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