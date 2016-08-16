package fr.liglab.adele.cream.runtime.handler.entity.behavior;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.runtime.handler.entity.utils.AbstractContextHandler;
import fr.liglab.adele.cream.runtime.handler.entity.utils.StateInterceptor;
import org.apache.felix.ipojo.ContextListener;
import org.apache.felix.ipojo.ContextSource;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Handler(name = HandlerReference.BEHAVIOR_ENTITY_HANDLER ,namespace = HandlerReference.NAMESPACE)
public class BehaviorEntityHandler extends AbstractContextHandler implements ContextSource{

    /**
     * The current values of the state properties
     */
    private final Map<String,Object> stateValues 		= new ConcurrentHashMap<>();

    private boolean instanceIsActive=false;

    /**
     * The list of iPOJO context listeners to notify on state updates.
     *
     * This handler implements ContextSource to allow state variables to be used in
     * dependency filters.
     */
    private final Set<ContextListener> contextSourceListeners	= new HashSet<>();

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

    }

    @Override
    public synchronized void stop() {
        contextSourceListeners.clear();
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
        boolean noChange 	= (oldValue == null && value == null) || (oldValue != null && value != null) && oldValue.equals(value);

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

    private void propagate(Hashtable<String,Object> properties){
        for (Map.Entry<String,Object> prop:  properties.entrySet()){
            notifyContextListener(prop.getKey(),prop.getValue());
        }
    }

    /**
     *
     * Context Entity Implementation
     *
     */

    @Override
    public Set<String> getServices() {
        return services;
    }

    @Override
    public String getId() {
        return (String) stateValues.get(CONTEXT_ENTITY_ID);
    }

    @Override
    public Object getStateValue(String state) {
        if (state == null)
            return null;

        return stateValues.get(state);
    }

    @Override
    public Set<String> getStates() {
        return new HashSet<>(stateIds);
    }

    @Override
    public Map<String, Object> dumpState() {
        return new HashMap<>(stateValues);
    }

    @Override
    public HandlerDescription getDescription() {
        return new BehaviorEntityHandlerDescription();
    }

    /**
     *Context Source Implementation
     */

    @Override
    public Object getProperty(String property) {
        return stateValues.get(property);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Dictionary getContext() {
        return new Hashtable<>(stateValues);
    }

    @Override
    public void registerContextListener(ContextListener listener, String[] properties) {
        if (!contextSourceListeners.contains(listener)){
            contextSourceListeners.add(listener);
        }
    }

    @Override
    public synchronized void unregisterContextListener(ContextListener listener) {
        contextSourceListeners.remove(listener);
    }

    /**
     * Notify All the context listener
     */
    private void notifyContextListener(String property,Object value){
        for (ContextListener listener : contextSourceListeners){
            listener.update(this,property,value);
        }
    }


    /**
     * The description of the handler.
     *
     * This class exposes the generic interface ContextEntity to allow external code to introspect the
     * component instance and obtain the current state values.
     *
     */
    public class BehaviorEntityHandlerDescription extends HandlerDescription implements ContextEntity {

        private BehaviorEntityHandlerDescription() {
            super(BehaviorEntityHandler.this);
        }

        @Override
        public Set<String> getServices() {
            return BehaviorEntityHandler.this.getServices();
        }

        @Override
        public String getId() {
            return BehaviorEntityHandler.this.getId();
        }

        @Override
        public Object getStateValue(String getStateValue) {
            return BehaviorEntityHandler.this.getStateValue(getStateValue);
        }

        @Override
        public Set<String> getStates() {
            return BehaviorEntityHandler.this.getStates();
        }

        @Override
        public Map<String, Object> dumpState() {
            return BehaviorEntityHandler.this.dumpState();
        }

        @Override
        public Element getHandlerInfo() {
            Element handlerInfo = super.getHandlerInfo();

            for (Map.Entry<String,Object> entry:dumpState().entrySet()){
                Element stateElement = new Element("state",null);
                stateElement.addAttribute(new Attribute(entry.getKey(),entry.getValue().toString()));
                handlerInfo.addElement(stateElement);
            }

            return handlerInfo;
        }
    }
}
