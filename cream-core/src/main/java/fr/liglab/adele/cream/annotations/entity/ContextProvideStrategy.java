package fr.liglab.adele.cream.annotations.entity;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.utils.CreamGenerator;
import fr.liglab.adele.cream.utils.CreamInvocationException;
import fr.liglab.adele.cream.utils.CustomInvocationHandler;
import fr.liglab.adele.cream.utils.SuccessorStrategy;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.Pojo;
import org.apache.felix.ipojo.handlers.providedservice.CreationStrategy;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
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
    private final Object lock = new Object();
    ClassLoader pojoClassLoader;
    /**
     * The instance manager passed to the iPOJO ServiceFactory to manage
     * instances.
     */
    private InstanceManager myManager;
    private List<Class<?>> classPublished = new ArrayList<>();

    @Override
    public void onPublication(InstanceManager instance, String[] interfaces, Properties props) {
        this.myManager = instance;

        pojoClassLoader = instance.getFactory().getBundleContext().getBundle().adapt(BundleWiring.class).getClassLoader();
        synchronized (lock) {
            classPublished.clear();

            for (String className : interfaces){
                try {
                    Class<?> clazz = pojoClassLoader.loadClass(className);
                    if (clazz != null){
                        classPublished.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    LOG.error("Unable to load interace " + className +" , the proxy will not implement this interace",e);
                }
            }
        }
    }

    @Override
    public void onUnpublication() {
        //Do nothing on publication
    }

	@Override
    @SuppressWarnings("rawtypes")
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration) {

        Object pojo = myManager.getPojoObject();

        List<InvocationHandler> successor = new ArrayList<>();

        InvocationHandler behaviorHandler = getBehaviorHandler();

        if (behaviorHandler != null) {
            successor.add(behaviorHandler);
        }

        InvocationHandler invocationHandler = new CustomInvocationHandler(pojo, (CreamGenerator) myManager,
                new ParentSuccessorStrategy(), successor
        );

        synchronized (lock) {

            Class[] arrayOfInterfaz = new Class[classPublished.size() + 1];

            arrayOfInterfaz = classPublished.toArray(arrayOfInterfaz);
            arrayOfInterfaz[classPublished.size()] = Pojo.class;
            try {
                return Proxy.newProxyInstance(pojoClassLoader, arrayOfInterfaz, invocationHandler);
            } catch (java.lang.NoClassDefFoundError e) {
                LOG.warn("Import-package declaration in bundle that contains instance " + myManager.getInstanceName() + " isn't enought explicit to load class defined in error. Context Provide strategy cannot be used, singleton strategy used instead ! ", e);
            }
        }

        return pojo;

    }

	@Override
    @SuppressWarnings("rawtypes")
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o) {
        //Do nothing on unget service
    }

    private InvocationHandler getBehaviorHandler() {
        Object handler = myManager.getHandler(HandlerReference.NAMESPACE + ":" + HandlerReference.FUNCTIONAL_EXTENSION_TRACKER_HANDLER);
        if (handler == null) {
            return null;
        }
        return (InvocationHandler) handler;
    }

    private class ParentSuccessorStrategy implements SuccessorStrategy {

        private static final String EQUALS_METHOD_CALL = "equals";

        private static final String HASHCODE_METHOD_CALL = "hashCode";

        private static final String GETCOMPONENTINSTANCE_METHOD_CALL = "getComponentInstance";

        private static final String TOSTRING_METHOD_CALL = "toString";

        private static final String NONE_OBJECT_METHOD_CALL = "None";

        @Override
        public Object successorStrategy(Object pojo, List<InvocationHandler> successors, Object proxy, Method method, Object[] args) {
            String nativeMethodCode = belongToObjectMethod(method, args);
            if (!NONE_OBJECT_METHOD_CALL.equals(nativeMethodCode)) {
                if (EQUALS_METHOD_CALL.equals(nativeMethodCode)) {
                    return proxy == args[0] || pojo.equals(args[0]);
                } else if (HASHCODE_METHOD_CALL.equals(nativeMethodCode)) {
                    return pojo.hashCode();
                } else if (TOSTRING_METHOD_CALL.equals(nativeMethodCode)) {
                    return pojo.toString();
                }
            }

            if (belongToPojoInterfaceMethod(method, args)) {
                return ((Pojo) pojo).getComponentInstance();
            }

            return applySuccessionStrategy(successors, proxy, method, args);

        }

        private String belongToObjectMethod(Method method, Object[] args) {
            if (TOSTRING_METHOD_CALL.equals(method.getName()) && args == null) {
                return TOSTRING_METHOD_CALL;
            }
            if (HASHCODE_METHOD_CALL.equals(method.getName()) && args == null) {
                return HASHCODE_METHOD_CALL;
            }
            if (EQUALS_METHOD_CALL.equals(method.getName()) && args.length == 1) {
                return EQUALS_METHOD_CALL;
            }
            return NONE_OBJECT_METHOD_CALL;
        }

        private boolean belongToPojoInterfaceMethod(Method method, Object[] args) {
            if (GETCOMPONENTINSTANCE_METHOD_CALL.equals(method.getName()) && args == null && Pojo.class.equals(method.getDeclaringClass())) {
                return true;
            }
            return false;
        }

        private Object applySuccessionStrategy(List<InvocationHandler> successors, Object proxy, Method method, Object[] args) {
            for (InvocationHandler successor : successors) {
                Object returnObj = null;
                try {
                    returnObj = successor.invoke(proxy, method, args);
                } catch (Throwable throwable) {
                    throw new CreamInvocationException("cause by", throwable);
                }
                if (SuccessorStrategy.NO_FOUND_CODE.equals(returnObj)) {
                    continue;
                }
                return returnObj;
            }
            LOG.warn("Cream invocation exception caused because " + method.getName() + " can not be found");
            throw new CreamInvocationException();
        }
    }
}
