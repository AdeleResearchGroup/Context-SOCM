package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.runtime.handler.behavior.manager.BehaviorHandler;
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
    protected Object createObject() {
        System.out.println(" Someone CREATE  OBJECT And His Proxy ! ");
        Object pojo = super.createObject();
        Class clazz = getClazz();

        Behavior[] behaviors = (Behavior[]) clazz.getAnnotationsByType(Behavior.class);
        Class<?> clazzInterface[] = clazz.getInterfaces();

        Class<?> interfaces[] = new Class[behaviors.length+clazzInterface.length];

        int i = 0;
        for (Behavior behavior:behaviors){
            Class service = behavior.spec();
            interfaces[i] = service;
            i++;
        }

        for (Class interfaz:clazzInterface){
            interfaces[i] = interfaz;
            i++;
        }

        List<InvocationHandler> successor = new ArrayList<InvocationHandler>();
        successor.add(getBehaviorHandler());

        InvocationHandler invocationHandler = new CustomInvocationHandler(pojo,this,
                new ParentSuccessorStrategy(),successor
        );

        return Proxy.newProxyInstance(clazz.getClassLoader(),interfaces,invocationHandler);
    }


    private class ParentSuccessorStrategy implements SuccessorStrategy{

        private final static String EQUALS_METHOD_CALL = "equals";

        private final static String HASHCODE_METHOD_CALL = "hashcode";

        private final static String TOSTRING_METHOD_CALL = "toString";

        private final static String NONE_OBJECT_METHOD_CALL = "None";
        @Override
        public Object successorStrategy(Object pojo,List<InvocationHandler> successors, Object proxy, Method method, Object[] args) throws Throwable {
            String nativeMethodCode = belongToObjectMethod(  proxy,  method, args);
            if (!NONE_OBJECT_METHOD_CALL.equals(nativeMethodCode)){
                if (EQUALS_METHOD_CALL.equals(nativeMethodCode)){
                    return pojo.equals(args[0]);
                }
                else if (HASHCODE_METHOD_CALL.equals(nativeMethodCode)){
                    return pojo.hashCode();
                }
                else if (TOSTRING_METHOD_CALL.equals(nativeMethodCode)){

                    return pojo.toString();
                }

            }

            for (InvocationHandler successor : successors){
                Object returnObj = successor.invoke(proxy,method,args);
                if (SuccessorStrategy.NO_FOUND_CODE.equals(returnObj)){
                    continue;
                }
                return returnObj;
            }
            throw  new RuntimeException();
        }

        private String belongToObjectMethod(Object proxy, Method method, Object[] args){
            if (TOSTRING_METHOD_CALL.equals(method.getName()) && args == null ){
                return TOSTRING_METHOD_CALL;
            }
            if (HASHCODE_METHOD_CALL.equals(method.getName()) && args == null ){
                return HASHCODE_METHOD_CALL;
            }
            if (EQUALS_METHOD_CALL.equals(method.getName()) && args.length == 1 ){
                return EQUALS_METHOD_CALL;
            }
            return NONE_OBJECT_METHOD_CALL;
        }
    }


}
