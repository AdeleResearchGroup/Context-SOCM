package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.runtime.handler.behavior.lifecycle.BehaviorLifecyleHandler;
import fr.liglab.adele.cream.runtime.handler.entity.behavior.BehaviorEntityHandler;
import fr.liglab.adele.cream.utils.CustomInvocationHandler;
import fr.liglab.adele.cream.utils.SuccessorStrategy;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

/**
 * Created by aygalinc on 31/05/16.
 */
public class BehaviorInstanceManager extends InstanceManager {

    private static final String LIFECYCLE_HANDLER = HandlerReference.NAMESPACE+":"+ HandlerReference.BEHAVIOR_LIFECYCLE_HANDLER;

    private String id;

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
            return SuccessorStrategy.NO_FOUND_CODE;
        }
    }

    public boolean isOperationnal(){
        for (Handler handler : getRegisteredHandlers()){
            HandlerFactory fact = (HandlerFactory) handler.getHandlerManager().getFactory();
            if (fact.getHandlerName().equals(LIFECYCLE_HANDLER)) {
                continue;
            }
            if (!handler.getValidity()){
                return false;
            }
        }
        return true;
    }

    public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
        if (configuration.get(BehaviorReference.BEHAVIOR_ID_CONFIG) == null){
            throw new ConfigurationException(BehaviorReference.BEHAVIOR_ID_CONFIG + "config parameter must be provided");
        }
        id = (String) configuration.get(BehaviorReference.BEHAVIOR_ID_CONFIG);
        super.configure(metadata,configuration);
    }


    /**
     * Gets the description of the current instance.
     *
     * @return the instance description.
     * @see org.apache.felix.ipojo.ComponentInstance#getInstanceDescription()
     */
    @Override
    public InstanceDescription getInstanceDescription() {
        InstanceDescription description = super.getInstanceDescription();
        if (description.getDescription().getAttribute(BehaviorReference.BEHAVIOR_ID_CONFIG) != null){
            description.getDescription().addAttribute(new Attribute(BehaviorReference.BEHAVIOR_ID_CONFIG,id));
        }
        return description;
    }

}
