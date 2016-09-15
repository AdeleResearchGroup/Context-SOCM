package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.annotations.behavior.BehaviorProvider;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.runtime.handler.behavior.lifecycle.BehaviorLifecyleHandler;
import fr.liglab.adele.cream.runtime.handler.entity.behavior.BehaviorEntityHandler;
import fr.liglab.adele.cream.utils.*;
import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.ContextListener;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.InstanceManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by aygalinc on 31/05/16.
 */
public class BehaviorInstanceManager extends InstanceManager implements CreamGenerator{

    private static final Logger LOG = LoggerFactory.getLogger(BehaviorInstanceManager.class);

    private static final String LIFECYCLE_HANDLER = HandlerReference.NAMESPACE+":"+ HandlerReference.BEHAVIOR_LIFECYCLE_HANDLER;

    private final CreamProxyFactory creamProxyFactory = new CreamProxyFactory(this.getClass().getClassLoader(),this);

    private Map<Method,GeneratedDelegatorProxy> proxyDelegatorMap = new HashMap<>();

    /**
     * Creates a new Component Manager.
     * The instance is not initialized.
     *
     * @param factory  the factory managing the instance manager
     * @param context  the bundle context to give to the instance
     * @param handlers handler object array
     */
    public BehaviorInstanceManager(ComponentFactory factory, BundleContext context, HandlerManager[] handlers) {
        super(factory, context, handlers);
    }

    public InvocationHandler getInvocationHandler(){
        Object pojo = getPojoObject();
        return new CustomInvocationHandler(pojo,this,new NotFoundStrategy(),new ArrayList<>());
    }

    public BehaviorLifecyleHandler getBehaviorLifeCycleHandler(){
        return (BehaviorLifecyleHandler)  getHandler(LIFECYCLE_HANDLER);
    }

    public void registerContextListenerToBehaviorEntityHandler(ContextListener listener){
        BehaviorEntityHandler entityHandler = (BehaviorEntityHandler)  getHandler(HandlerReference.NAMESPACE+":"+HandlerReference.BEHAVIOR_ENTITY_HANDLER);
        if (entityHandler == null){
            return;
        }
        entityHandler.registerContextListener(listener,null);
    }

    public void unregisterContextListenerToBehaviorEntityHandler(ContextListener listener){
        BehaviorEntityHandler entityHandler = (BehaviorEntityHandler)  getHandler(HandlerReference.NAMESPACE+":"+HandlerReference.BEHAVIOR_ENTITY_HANDLER);
        if (entityHandler == null){
            return;
        }
        entityHandler.unregisterContextListener(listener);
    }


    private class NotFoundStrategy implements SuccessorStrategy{

        @Override
        public Object successorStrategy(Object pojo,List<InvocationHandler> successors, Object proxy, Method method, Object[] args){
            return SuccessorStrategy.NO_FOUND_CODE;
        }
    }

    public Map<Method,GeneratedDelegatorProxy> getProxyDelegationMap(){
        if (proxyDelegatorMap.isEmpty()){
            Class clazz = getClazz();
            BehaviorProvider[] behaviors = (BehaviorProvider[]) clazz.getAnnotationsByType(BehaviorProvider.class);
            for (BehaviorProvider provider:behaviors){
                Class behaviorService = provider.spec();
                Method[] methods = behaviorService.getMethods();
                if ((methods == null)||(methods.length == 0)){
                    break;
                }
                GeneratedDelegatorProxy proxy = (GeneratedDelegatorProxy) creamProxyFactory.getProxy(behaviorService);
                for (Method method : methods){
                    proxyDelegatorMap.put(method,proxy);
                }
            }
        }
        return proxyDelegatorMap;
    }
}
