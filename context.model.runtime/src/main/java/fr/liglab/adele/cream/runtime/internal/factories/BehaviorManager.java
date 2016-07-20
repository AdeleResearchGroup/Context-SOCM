package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.runtime.handler.behavior.lifecycle.BehaviorLifecyleHandler;
import fr.liglab.adele.cream.runtime.handler.entity.behavior.BehaviorEntityHandler;
import fr.liglab.adele.cream.utils.CustomInvocationHandler;
import fr.liglab.adele.cream.utils.SuccessorStrategy;
import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.ContextListener;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.InstanceManager;
import org.osgi.framework.BundleContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aygalinc on 31/05/16.
 */
public class BehaviorManager extends InstanceManager {

    /**
     * Creates a new Component Manager.
     * The instance is not initialized.
     *
     * @param factory  the factory managing the instance manager
     * @param context  the bundle context to give to the instance
     * @param handlers handler object array
     */
    public BehaviorManager(ComponentFactory factory, BundleContext context, HandlerManager[] handlers) {
        super(factory, context, handlers);
    }

    public InvocationHandler getInvocationHandler(){
        Object pojo = getPojoObject();
        return new CustomInvocationHandler(pojo,this,new NotFoundStrategy(),new ArrayList<>());
    }

    public BehaviorLifecyleHandler getBehaviorLifeCycleHandler(){
        return (BehaviorLifecyleHandler)  getHandler(BehaviorReference.BEHAVIOR_NAMESPACE+":"+BehaviorReference.BEHAVIOR_LIFECYCLE_NAME);
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
        public Object successorStrategy(Object pojo,List<InvocationHandler> successors, Object proxy, Method method, Object[] args) throws Throwable {
            return SuccessorStrategy.NO_FOUND_CODE;
        }
    }
}
