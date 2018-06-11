package fr.liglab.adele.cream.runtime.handler.entity;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.runtime.handler.entity.utils.ContextStateHandler;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

import java.util.Dictionary;

@Handler(name = HandlerReference.ENTITY_HANDLER, namespace = HandlerReference.NAMESPACE)
public class EntityStateHandler extends ContextStateHandler implements ContextSource {

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
