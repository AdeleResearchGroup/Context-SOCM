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

import java.util.Collections;
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

    @Override
    protected void notifyContextListeners(String property, Object oldValue, Object value) {
    	
    	super.notifyContextListeners(property, oldValue, value);

    	/*
    	 * propagate value to provided handler
    	 */
        propagate(property, oldValue, value);
    }


    /**
     * The provider handler of my associated iPOJO component instance
     */
    private ProvidedServiceHandler providerHandler;

    /**
     * Propagate a state value change to the published properties of the context spec
     */
    private void propagate(String state, Object oldValue, Object value) {

        if (providerHandler == null)
            return;

        Hashtable<String, Object> property = new Hashtable<>();
        property.put(state, value != null ? value : "TO_BE_REMOVED" );

        if (value == null) {
            providerHandler.removeProperties(property);
        }
 
        if (value != null) {
            providerHandler.addProperties(property);
        }
         
    }

    

    @Override
    public synchronized void start() {
    	
    	providerHandler = (ProvidedServiceHandler) getHandler(HandlerFactory.IPOJO_NAMESPACE + ":provides");
    	super.start();
        
    }

    @Override
    public synchronized void stop() {

    	super.stop();
        providerHandler = null;

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
