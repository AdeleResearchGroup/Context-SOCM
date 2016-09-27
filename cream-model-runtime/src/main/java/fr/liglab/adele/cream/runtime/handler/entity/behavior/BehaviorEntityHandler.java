package fr.liglab.adele.cream.runtime.handler.entity.behavior;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.runtime.handler.entity.utils.AbstractContextHandler;
import fr.liglab.adele.cream.runtime.handler.entity.utils.StateInterceptor;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.ContextSource;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.metadata.Element;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

@Handler(name = HandlerReference.BEHAVIOR_ENTITY_HANDLER ,namespace = HandlerReference.NAMESPACE)
public class BehaviorEntityHandler extends AbstractContextHandler implements ContextEntity,ContextSource{

    private boolean instanceIsActive=false;

    @Override
    protected boolean isInstanceActive() {
        return instanceIsActive;
    }

    /**
     * The Wisdom Scheduler used to handle periodic tasks
     */
    @Requires(id="scheduler",proxy = false)
    public ManagedScheduledExecutorService scheduler;


    @Override
    protected ManagedScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     *
     * Handler Lifecycle
     */
    @Override
    public synchronized void stateChanged(int state) {

        if (state == InstanceManager.VALID) {
            instanceIsActive = true;

            for (Map.Entry<String,Object> initialEntry : this.getInitialConfiguration().entrySet()){
                update(initialEntry.getKey(),initialEntry.getValue());
            }

            /*
             * restart state handlers
             */
            for (StateInterceptor interceptor : interceptors) {
                interceptor.validate();
            }
        }

        if (state == InstanceManager.INVALID) {
            instanceIsActive = false;

            stateValues.clear();
            Map<String,Object> stateToNull = new HashMap<>();
            for (String stateId : stateIds){
                stateToNull.put(stateId,null);
            }
            propagate(new HashMap<>(stateToNull));
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
//Do nothing
    }

    @Override
    public void configure(Element element, Dictionary dictionary) throws ConfigurationException {
        super.configure(element,dictionary,HandlerReference.NAMESPACE,HandlerReference.BEHAVIOR_ENTITY_HANDLER);
    }

    @Override
    public synchronized void stop() {
        super.stop();
    }


    /**
     * Handler Propagation
     */


    /**
     * Updates the value of a state property, propagating the change to the published service properties
     * @param stateId
     * @param value
     */
    @Override
    public void update(String stateId, Object value) {

        assert stateId != null && stateIds.contains(stateId);

        Object oldValue 	= stateValues.get(stateId);
        boolean bothNull 	= oldValue == null && value == null;
        boolean equals = (oldValue != null && value != null) && oldValue.equals(value);
        boolean noChange = bothNull && equals;

        if (noChange)
            return;

        if (value != null) {
            stateValues.put(stateId, value);
        }
        else {
            stateValues.remove(stateId);
        }

        if (CONTEXT_ENTITY_ID.equals(stateId)){
            return;
        }

        notifyContextListener(stateId,value);
    }

    private void propagate(Map<String,Object> properties){
        for (Map.Entry<String,Object> prop:  properties.entrySet()){
            notifyContextListener(prop.getKey(),prop.getValue());
        }
    }







}
