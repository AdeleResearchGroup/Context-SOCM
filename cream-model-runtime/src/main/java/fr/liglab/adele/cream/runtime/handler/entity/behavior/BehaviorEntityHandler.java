package fr.liglab.adele.cream.runtime.handler.entity.behavior;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.model.ContextEntity;

import fr.liglab.adele.cream.runtime.handler.entity.utils.ContextStateHandler;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.ContextSource;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.metadata.Element;

import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

import java.util.Dictionary;

@Handler(name = HandlerReference.FUNCTIONAL_EXTENSION_ENTITY_HANDLER, namespace = HandlerReference.NAMESPACE)
public class BehaviorEntityHandler extends ContextStateHandler implements ContextEntity, ContextSource {

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
        super.configure(element, dictionary, HandlerReference.NAMESPACE, HandlerReference.FUNCTIONAL_EXTENSION_ENTITY_HANDLER);
    }

}
