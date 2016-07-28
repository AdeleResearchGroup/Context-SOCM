package fr.liglab.adele.cream.annotations.entity;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.utils.CustomInvocationHandler;
import fr.liglab.adele.cream.utils.SuccessorStrategy;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.handlers.providedservice.CreationStrategy;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(ContextProvideStrategy.class);

    /**
     * The instance manager passed to the iPOJO ServiceFactory to manage
     * instances.
     */
    private InstanceManager myManager;

    @Override
    public void onPublication(InstanceManager instance, String[] interfaces, Properties props) {
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
        try{
            pojo = Proxy.newProxyInstance(clazz.getClassLoader(),interfaces,invocationHandler);
        }catch (java.lang.NoClassDefFoundError e){
            LOG.warn("Import-package declaration in bundle that contains instance " + myManager.getInstanceName() + " isn't enought explicit to load class defined in error. Context Provide strategy cannot be used, singleton strategy used instead ! ",e);
        }
        return pojo;
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o) {

    }

    private InvocationHandler getBehaviorHandler(){
        Object  handler = myManager.getHandler(HandlerReference.NAMESPACE+":"+ HandlerReference.BEHAVIOR_MANAGER_HANDLER);
        if (handler == null){
            return null;
        }
        return (InvocationHandler)  handler;
    }

    private class ParentSuccessorStrategy implements SuccessorStrategy {

        private static final String EQUALS_METHOD_CALL = "equals";

        private static final String HASHCODE_METHOD_CALL = "hashcode";

        private static final String TOSTRING_METHOD_CALL = "toString";

        private static final String NONE_OBJECT_METHOD_CALL = "None";
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
