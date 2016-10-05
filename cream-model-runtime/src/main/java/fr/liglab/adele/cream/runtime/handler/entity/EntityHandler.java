package fr.liglab.adele.cream.runtime.handler.entity;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.runtime.handler.entity.utils.AbstractContextHandler;
import fr.liglab.adele.cream.runtime.handler.entity.utils.StateInterceptor;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceController;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

@Handler(name =HandlerReference.ENTITY_HANDLER ,namespace = HandlerReference.NAMESPACE)
@Provides(specifications = ContextEntity.class)
public class EntityHandler extends AbstractContextHandler implements ContextEntity, ContextSource {

	/**
	 * The provider handler of my associated iPOJO component instance
	 */
	private ProvidedServiceHandler providerHandler;

	/**
	 * service controller to align life-cycle of the generic ContextEntity service with
	 * the life-cycle of the domain-specific context services of the entity
	 */
	@ServiceController(value=false, specification=ContextEntity.class)
	private boolean instanceIsActive;

	/**
	 * The Wisdom Scheduler used to handle periodic tasks
	 */
	@Requires(id="scheduler",proxy = false)
	public ManagedScheduledExecutorService scheduler;

	@Override
	protected boolean isInstanceActive() {
		return instanceIsActive;
	}

	@Override
	protected ManagedScheduledExecutorService getScheduler() {
		return scheduler;
	}

	/**
	 * Updates the value of a state property, propagating the change to the published service properties
	 */
	@Override
	public void update(String stateId, Object value) {

		if(stateId == null && !stateIds.contains(stateId)){
			return;
		}

		Object oldValue 	= stateValues.get(stateId);
		boolean bothNull 	= oldValue == null && value == null;
		boolean equals = (oldValue != null && value != null) && oldValue.equals(value);
		boolean noChange = bothNull || equals;


		if (noChange) {
			return;
		}

		if (value != null) {
			stateValues.put(stateId, value);
		}
		else {
			stateValues.remove(stateId);
		}

		propagate(stateId,value,oldValue != null);
	}

	/**
	 * Propagate a state value change to the published properties of the context services
	 */
	private void propagate(String stateId, Object value, boolean isUpdate) {

		if (providerHandler == null)
			return;

		if (getInstanceManager().getState() <= InstanceManager.INVALID)
			return;

		Hashtable<String,Object> property = new Hashtable<>();
		property.put(stateId, value);

		if ( value != null && isUpdate) {
			providerHandler.reconfigure(property);
		}
		if ( value != null && !isUpdate) {
			providerHandler.addProperties(property);
		}
		else if (value == null) {
			providerHandler.removeProperties(property);

		}
		notifyContextListener(stateId,value);
	}

	private void propagate(Dictionary<String,Object> properties) {
		if (providerHandler == null)
			return;

		providerHandler.addProperties(properties);

		Enumeration<String> states = properties.keys();
		while(states.hasMoreElements()) {
			String state = states.nextElement();
			notifyContextListener(state,properties.get(state));
		}

	}

	@Override
	public synchronized void stateChanged(int state) {

		if (state == InstanceManager.VALID) {
			instanceIsActive = true;

			propagate(new Hashtable<>(stateValues));

            /*
             * restart state handlers
             */
			for (StateInterceptor interceptor : interceptors) {
				interceptor.validate();
			}
		}

		if (state == InstanceManager.INVALID) {
			instanceIsActive = false;

            /*
             * stop state handlers
             */
			for (StateInterceptor interceptor : interceptors) {
				interceptor.invalidate();
			}

		}
	}

	@Override
	public synchronized void start() {
		providerHandler = (ProvidedServiceHandler) getHandler(HandlerFactory.IPOJO_NAMESPACE + ":provides");
	}

	@Override
	public synchronized void stop() {
		super.stop();
		providerHandler = null;
	}

	/**
	 * Given an iPOJO instance with the entity handler attached, return the associated context entity
	 */
	public static ContextEntity getContextEntity(ComponentInstance instance) {

		if (instance == null) {
			return null;
		}

		String handlerId 						= HandlerReference.NAMESPACE+":"+HandlerReference.ENTITY_HANDLER;
		HandlerDescription handlerDescription	= instance.getInstanceDescription().getHandlerDescription(handlerId);

		if (handlerDescription instanceof EntityHandlerDescription) {
			return (EntityHandlerDescription) handlerDescription;
		}

		return null;
	}

	/**
	 * Given an iPOJO object with the entity handler attached, return the associated context entity
	 */
	public static ContextEntity getContextEntity(Pojo pojo) {
		return getContextEntity(pojo.getComponentInstance());
	}

	@Override
	public void configure(Element element, Dictionary dictionary) throws ConfigurationException {
		super.configure(element,dictionary,HandlerReference.NAMESPACE,HandlerReference.ENTITY_HANDLER);
	}

}
