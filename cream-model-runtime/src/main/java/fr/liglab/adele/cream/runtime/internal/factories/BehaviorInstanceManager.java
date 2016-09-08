package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.runtime.handler.behavior.lifecycle.BehaviorLifecyleHandler;
import fr.liglab.adele.cream.runtime.handler.entity.behavior.BehaviorEntityHandler;
import fr.liglab.adele.cream.utils.CustomInvocationHandler;
import fr.liglab.adele.cream.utils.MethodInvocationUtils;
import fr.liglab.adele.cream.utils.SuccessorStrategy;
import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.ContextListener;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.InstanceManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aygalinc on 31/05/16.
 */
public class BehaviorInstanceManager extends InstanceManager {

    private static final Logger LOG = LoggerFactory.getLogger(BehaviorInstanceManager.class);

    private static final String LIFECYCLE_HANDLER = HandlerReference.NAMESPACE+":"+ HandlerReference.BEHAVIOR_LIFECYCLE_HANDLER;

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

    public void registerBehaviorListener(ContextListener listener){
        BehaviorEntityHandler entityHandler = (BehaviorEntityHandler)  getHandler(HandlerReference.NAMESPACE+":"+HandlerReference.BEHAVIOR_ENTITY_HANDLER);
        if (entityHandler == null){
            return;
        }
        entityHandler.registerContextListener(listener,null);
    }

    private class NotFoundStrategy implements SuccessorStrategy{

        @Override
        public Object successorStrategy(Object pojo,List<InvocationHandler> successors, Object proxy, Method method, Object[] args){
            if (MethodInvocationUtils.isInvocableByReflexion(method,pojo)){
                try {

                    return MethodInvocationUtils.invokeByReflexion(method,pojo,proxy,args);
                }catch (Throwable throwable){
                    LOG.warn("invoke by reflexion cause an exception,return a no found code",throwable);
                }
            }
            return SuccessorStrategy.NO_FOUND_CODE;
        }
    }
}
