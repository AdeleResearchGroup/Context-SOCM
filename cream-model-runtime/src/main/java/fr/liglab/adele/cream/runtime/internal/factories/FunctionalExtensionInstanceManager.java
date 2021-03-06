package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.runtime.handler.entity.behavior.BehaviorEntityHandler;
import fr.liglab.adele.cream.runtime.handler.functional.extension.lifecycle.FunctionalExtensionLifecyleHandler;
import fr.liglab.adele.cream.utils.*;
import org.apache.felix.ipojo.*;
import org.osgi.framework.BundleContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by aygalinc on 31/05/16.
 */
public class FunctionalExtensionInstanceManager extends InstanceManager implements CreamGenerator {

    private static final String LIFECYCLE_HANDLER = HandlerReference.NAMESPACE + ":" + HandlerReference.FUNCTIONAL_EXTENSION_LIFECYCLE_HANDLER;

    private final CreamProxyFactory creamProxyFactory = new CreamProxyFactory(this.getClass().getClassLoader(), this);

    private Map<Method, GeneratedDelegatorProxy> proxyDelegatorMap = new HashMap<>();

    /**
     * Creates a new Component Manager.
     * The instance is not initialized.
     *
     * @param factory  the factory managing the instance manager
     * @param context  the bundle context to give to the instance
     * @param handlers handler object array
     */
    public FunctionalExtensionInstanceManager(ComponentFactory factory, BundleContext context, HandlerManager[] handlers) {
        super(factory, context, handlers);
    }

    public InvocationHandler getInvocationHandler() {
        Object pojo = getPojoObject();
        return new CustomInvocationHandler(pojo, this, new NotFoundStrategy(), new ArrayList<>());
    }

    public FunctionalExtensionLifecyleHandler getBehaviorLifeCycleHandler() {
        return (FunctionalExtensionLifecyleHandler) getHandler(LIFECYCLE_HANDLER);
    }

    public ContextSource getExtensionContextSource() {
        Handler handler = getHandler(HandlerReference.NAMESPACE + ":" + HandlerReference.FUNCTIONAL_EXTENSION_ENTITY_HANDLER);
        if (handler instanceof ContextSource) {
            return (ContextSource) handler;
        } else {
            return null;
        }
    }

    public void registerContextListenerToExtensionEntityHandler(ContextListener listener) {
        BehaviorEntityHandler entityHandler = (BehaviorEntityHandler) getHandler(HandlerReference.NAMESPACE + ":" + HandlerReference.FUNCTIONAL_EXTENSION_ENTITY_HANDLER);
        if (entityHandler == null) {
            return;
        }
        entityHandler.registerContextListener(listener, null);
    }

    public void unregisterContextListenerToExtensionEntityHandler(ContextListener listener) {
        BehaviorEntityHandler entityHandler = (BehaviorEntityHandler) getHandler(HandlerReference.NAMESPACE + ":" + HandlerReference.FUNCTIONAL_EXTENSION_ENTITY_HANDLER);
        if (entityHandler == null) {
            return;
        }
        entityHandler.unregisterContextListener(listener);
    }
    
    @Override
    public Map<Method, GeneratedDelegatorProxy> getProxyDelegationMap() {
        if (proxyDelegatorMap.isEmpty()) {
            Class clazz = getClazz();
            FunctionalExtender[] behaviors = (FunctionalExtender[]) clazz.getAnnotationsByType(FunctionalExtender.class);
            for (FunctionalExtender provider : behaviors) {
                Class[] behaviorServices = provider.contextServices();
                Set<Class> setOfBehaviorService = new HashSet<>(Arrays.asList(behaviorServices));
                proxyDelegatorMap = ProxyGeneratorUtils.getGeneratedProxyByMethodMap(setOfBehaviorService, creamProxyFactory);
            }
        }
        return proxyDelegatorMap;
    }

    private class NotFoundStrategy implements SuccessorStrategy {

        @Override
        public Object successorStrategy(Object pojo, List<InvocationHandler> successors, Object proxy, Method method, Object[] args) {
            return SuccessorStrategy.NO_FOUND_CODE;
        }
    }
}
