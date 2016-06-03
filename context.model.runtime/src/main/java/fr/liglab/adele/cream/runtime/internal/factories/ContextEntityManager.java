package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.runtime.handler.behavior.BehaviorHandler;
import fr.liglab.adele.cream.runtime.internal.utils.CustomInvocationHandler;
import fr.liglab.adele.cream.runtime.internal.utils.SuccessorStrategy;
import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

/**
 * Created by aygalinc on 31/05/16.
 */
public class ContextEntityManager extends InstanceManager{


    /**
     * Creates a new Component Manager.
     * The instance is not initialized.
     *
     * @param factory  the factory managing the instance manager
     * @param context  the bundle context to give to the instance
     * @param handlers handler object array
     */
    public ContextEntityManager(ComponentFactory factory, BundleContext context, HandlerManager[] handlers) {
        super(factory, context, handlers);

    }

    public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
        super.configure(metadata,configuration);
    }


    private BehaviorHandler getBehaviorHandler(){
        return (BehaviorHandler)  getHandler(BehaviorReference.BEHAVIOR_NAMESPACE+":"+BehaviorReference.DEFAULT_BEHAVIOR_TYPE);
    }


    @Override
    public Object createPojoObject(){
        System.out.println(" Someone Get a proxy ! ");
        Object pojo = super.createPojoObject();

        Class clazz = getClazz();
        Behavior[] behaviors = (Behavior[]) clazz.getAnnotationsByType(Behavior.class);
        List<Class> interfaz = new ArrayList<>();
        for (Behavior behavior:behaviors){
            Class service = behavior.spec();
            interfaz.add(service);
        }

        List<InvocationHandler> successor = new ArrayList<InvocationHandler>();
        successor.add(getBehaviorHandler());

        InvocationHandler invocationHandler = new CustomInvocationHandler(pojo,this,
                new ParentSuccessorStrategy(),successor
        );

        return Proxy.newProxyInstance(clazz.getClassLoader(),(Class<?>[]) interfaz.toArray(),invocationHandler);
    }


    private class ParentSuccessorStrategy implements SuccessorStrategy{

        @Override
        public Object successorStrategy(List<InvocationHandler> successors, Object proxy, Method method, Object[] args) throws Throwable {
            for (InvocationHandler successor : successors){
                Object returnObj = successor.invoke(proxy,method,args);
                if (SuccessorStrategy.NO_FOUND_CODE.equals(returnObj)){
                    continue;
                }
                return returnObj;
            }
            throw  new RuntimeException();
        }
    }

}
