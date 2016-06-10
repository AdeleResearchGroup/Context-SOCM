package fr.liglab.adele.cream.annotations.strategy;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.utils.CustomInvocationHandler;
import fr.liglab.adele.cream.utils.SuccessorStrategy;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.handlers.providedservice.CreationStrategy;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by aygalinc on 10/06/16.
 */
public class ContextProvideStrategy extends CreationStrategy {

    /**
     * The instance manager passed to the iPOJO ServiceFactory to manage
     * instances.
     */
    private InstanceManager myManager;

    /**
     * The lists of interfaces provided by this service.
     */
    private String[] mySpecs;

    @Override
    public void onPublication(InstanceManager instance, String[] interfaces, Properties props) {
        this.mySpecs = interfaces;
        this.myManager = instance;
    }

    @Override
    public void onUnpublication() {

    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration) {

        Object pojo = myManager.getPojoObject();
        Class clazz = myManager.getClazz();

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

        InvocationHandler behaviorHandler = getBehaviorHandler();

        if (behaviorHandler != null){
            successor.add(behaviorHandler);
        }

        InvocationHandler invocationHandler = new CustomInvocationHandler(pojo,myManager,
                new ParentSuccessorStrategy(),successor
        );

        return Proxy.newProxyInstance(clazz.getClassLoader(),interfaces,invocationHandler);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o) {

    }

    private InvocationHandler getBehaviorHandler(){
        Object  handler = myManager.getHandler(BehaviorReference.BEHAVIOR_NAMESPACE+":"+BehaviorReference.DEFAULT_BEHAVIOR_TYPE);
        if (handler == null) return null;
        return (InvocationHandler)  handler;
    }

    private class ParentSuccessorStrategy implements SuccessorStrategy {

        private final static String EQUALS_METHOD_CALL = "equals";

        private final static String HASHCODE_METHOD_CALL = "hashcode";

        private final static String TOSTRING_METHOD_CALL = "toString";

        private final static String NONE_OBJECT_METHOD_CALL = "None";
        @Override
        public Object successorStrategy(Object pojo, List<InvocationHandler> successors, Object proxy, Method method, Object[] args) throws Throwable {
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
