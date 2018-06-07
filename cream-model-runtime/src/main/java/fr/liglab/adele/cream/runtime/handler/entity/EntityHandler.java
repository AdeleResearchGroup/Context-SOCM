package fr.liglab.adele.cream.runtime.handler.entity;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.runtime.handler.entity.utils.ContextStateHandler;

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
import java.util.Hashtable;
import java.util.Map;

@Handler(name = HandlerReference.ENTITY_HANDLER, namespace = HandlerReference.NAMESPACE)
@Provides(specifications = ContextEntity.class)
public class EntityHandler extends ContextStateHandler implements ContextEntity, ContextSource {

    /**
     * The Wisdom Scheduler used to handle periodic tasks
     */
    @Requires(id = "scheduler", proxy = false, optional = false)
    public ManagedScheduledExecutorService scheduler;
   
    @Override
    protected ManagedScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public void configure(Element element, Dictionary dictionary) throws ConfigurationException {
        super.configure(element, dictionary, HandlerReference.NAMESPACE, HandlerReference.ENTITY_HANDLER);
    }

    
    /**
     * service controller to align life-cycle of the generic ContextEntity service with
     * the life-cycle of the domain-specific context spec of the entity
     */
    @ServiceController(value = false, specification = ContextEntity.class)
    private boolean instanceIsActive;

    /**
     * The provider handler of my associated iPOJO component instance
     */
    private ProvidedServiceHandler providerHandler;

    /**
     * Propagate a state value change to the published properties of the context spec
     */
    private void propagate(String stateId, Object value, boolean isUpdate) {

        if (providerHandler == null)
            return;

        Hashtable<String, Object> property = new Hashtable<>();
        property.put(stateId, value);

        if (value != null && isUpdate) {
            providerHandler.reconfigure(property);
        }
        else if (value != null && !isUpdate) {
            providerHandler.addProperties(property);
        }
        else if (value == null) {
            providerHandler.removeProperties(property);

        }
    }

    /**
     * Updates the value of a state property, propagating the change to the published service properties
     */
    @Override
    protected boolean update(String stateId, Object value) {

    	Object oldValue	= getValue(stateId);
    	boolean updated = super.update(stateId,value);

    	if (updated) {
            propagate(stateId, value, oldValue != null);
    	}
    	
    	return updated;
    }

    @Override
    public synchronized void start() {
    	super.start();
    	
        providerHandler = (ProvidedServiceHandler) getHandler(HandlerFactory.IPOJO_NAMESPACE + ":provides");
        
    	for (Map.Entry<String,Object> state : stateValues.entrySet()) {
    		propagate(state.getKey(),state.getValue(),false);
		}

    }

    @Override
    public synchronized void stop() {
        providerHandler = null;
        super.stop();
    }

    @Override
    public synchronized void stateChanged(int state) {

    	instanceIsActive = state == InstanceManager.VALID;

        super.stateChanged(state);
    }


    /**
     * Given an iPOJO instance with the entity handler attached, return the associated context entity
     */
    public static ContextEntity getContextEntity(ComponentInstance instance) {

        if (instance == null) {
            return null;
        }

        String handlerId = HandlerReference.NAMESPACE + ":" + HandlerReference.ENTITY_HANDLER;
        HandlerDescription handlerDescription = instance.getInstanceDescription().getHandlerDescription(handlerId);

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


}
