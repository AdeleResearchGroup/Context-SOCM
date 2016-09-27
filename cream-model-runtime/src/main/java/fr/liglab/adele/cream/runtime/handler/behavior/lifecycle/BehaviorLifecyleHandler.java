package fr.liglab.adele.cream.runtime.handler.behavior.lifecycle;

import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.apache.felix.ipojo.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

//Same level as provided handler, maybe to change ...
@Handler(name = HandlerReference.BEHAVIOR_LIFECYCLE_HANDLER, namespace = HandlerReference.NAMESPACE,level = 3)
public class BehaviorLifecyleHandler extends PrimitiveHandler implements ContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(BehaviorLifecyleHandler.class);

    private final Object myLock = new Object();

    private final List<BehaviorStateListener> stateListeners = new ArrayList<>();

    private final Map<Callback,String> listenerCallBack = new HashMap<>();

    private final Set<String> propertyToListen = new HashSet<>();

    private final InstanceStateListener privateInstanceListener = new InstanceListenerImpl();

    private String id;


    // TODO : some workaround method invocation, check if the parameter is a primitive type and not call the method if the argument is null,
    // When get the callback , issue can be found if the pojo own two method with the same name but different attributes(nbr or type), the pojometadata.getMethod() return
    // the first method with the good name...
    @Override
    public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
        //Do nothing
        if (configuration.get(BehaviorReference.BEHAVIOR_ID_CONFIG) == null){
            throw new ConfigurationException(BehaviorReference.BEHAVIOR_ID_CONFIG + "config parameter must be provided");
        }
        id = (String) configuration.get(BehaviorReference.BEHAVIOR_ID_CONFIG);

        Element[] elements = metadata.getElements(HandlerReference.BEHAVIOR_LIFECYCLE_HANDLER,HandlerReference.NAMESPACE);
        if (elements == null || elements.length == 0){
            return;
        }

        PojoMetadata pojoMetadata = getPojoMetadata();
        for (Element element : elements){
            Element[] propertyElement = element.getElements();

            for (Element property : propertyElement) {
                String stateId = property.getAttribute("id");
                String methodCallbackId = property.getAttribute("method");
                MethodMetadata methodMetadata = pojoMetadata.getMethod(methodCallbackId);

                if (methodMetadata == null) {
                    throw new ConfigurationException(" method metadata is null for method " + methodCallbackId);
                }

                Callback methodCallBack = new Callback(methodMetadata, getInstanceManager());
                listenerCallBack.put(methodCallBack, stateId);
                propertyToListen.add(stateId);
            }
        }

    }

    @Override
    public synchronized void stop() {
        getInstanceManager().removeInstanceStateListener(privateInstanceListener);
    }

    @Override
    public synchronized void start() {
        setValidity(false);
        getInstanceManager().addInstanceStateListener(privateInstanceListener);
    }



    public synchronized void registerBehaviorListener(BehaviorStateListener listener) {
        stateListeners.add(listener);
    }

    public synchronized void unregisterBehaviorListener(BehaviorStateListener listener) {
        stateListeners.remove(listener);
    }


    public void stopBehavior() {
        synchronized (myLock) {
            if (this.getValidity()) {
                setValidity(false);
            }
        }
    }

    public void startBehavior(){
        synchronized (myLock) {
            if (!this.getValidity()) {
                setValidity(true);
            }
        }
    }

    private void notifyListener(int state){
        for (BehaviorStateListener behaviorStateListener: stateListeners){
            behaviorStateListener.behaviorStateChange(state,id);
        }
    }

    @Override
    public HandlerDescription getDescription() {
        return new BehaviorLifecycleHandlerDescription(this);
    }

    @Override
    public void update(ContextSource source, String property, Object value) {
        if (getInstanceManager().getState() != ComponentInstance.VALID){
            return;
        }
        if (!listenerCallBack.containsValue(property)){
            return;
        }
        Object[] args = new Object[]{value};

        for (Map.Entry<Callback,String> callback : listenerCallBack.entrySet()) {
            try {
                if (callback.getValue().equals(property)) {
                    callback.getKey().call(args);
                }
            } catch (NoSuchMethodException e) {
                LOG.error("Error occurs during callback invocation of property : " + property + " cause by ", e);
            } catch (IllegalAccessException e) {
                LOG.error("Error occurs during callback invocation of property : " + property + " cause by ", e);
            } catch (InvocationTargetException e) {
                LOG.error("Error occurs during callback invocation of property : " + property + " cause by ", e);
            }
        }
    }


    public Set<String> getPropertiesToListen(){
        return propertyToListen;
    }

    private class BehaviorLifecycleHandlerDescription extends HandlerDescription{

        /**
         * Creates a handler description.
         *
         * @param handler the handler.
         */
        public BehaviorLifecycleHandlerDescription(org.apache.felix.ipojo.Handler handler) {
            super(handler);
        }

        @Override
        public Element getHandlerInfo() {
            Element element = super.getHandlerInfo();
            element.addAttribute(new Attribute(BehaviorReference.BEHAVIOR_ID_CONFIG,id));
            return element;
        }
    }

    /**
     * Ensure that all handler state method are called before notify the behavior tracker manager to expose behavior as a service if the componentbecome valid
     */
    private class InstanceListenerImpl implements InstanceStateListener{


        @Override
        public void stateChanged(ComponentInstance instance, int newState) {
            if (newState == ComponentInstance.VALID) {
                notifyListener(newState);
            }
        }
    }

    @Override
    public void stateChanged(int state) {
        if (state != ComponentInstance.VALID){
            notifyListener(state);
        }
    }
}
