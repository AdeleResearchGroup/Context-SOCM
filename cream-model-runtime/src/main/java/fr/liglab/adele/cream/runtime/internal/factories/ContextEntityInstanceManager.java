package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.utils.CreamGenerator;
import fr.liglab.adele.cream.utils.CreamProxyFactory;
import fr.liglab.adele.cream.utils.GeneratedDelegatorProxy;
import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.InstanceManager;
import org.osgi.framework.BundleContext;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by aygalinc on 31/05/16.
 */
public class ContextEntityInstanceManager extends InstanceManager implements CreamGenerator {

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
    public ContextEntityInstanceManager(ComponentFactory factory, BundleContext context, HandlerManager[] handlers) {
        super(factory, context, handlers);

    }

    public Map<Method, GeneratedDelegatorProxy> getProxyDelegationMap() {
        if (proxyDelegatorMap.isEmpty()) {
            Class clazz = getClazz();
            ContextEntity[] contextEntities = (ContextEntity[]) clazz.getAnnotationsByType(ContextEntity.class);
            for (ContextEntity entity : contextEntities) {
                Class[] entityClass = entity.services();
                Set<Class> classesFlatten = flattenClass(entityClass);
                proxyDelegatorMap = ProxyGeneratorUtils.getGeneratedProxyByMethodMap(classesFlatten, creamProxyFactory);
            }
        }
        return proxyDelegatorMap;
    }

    private Set<Class> flattenClass(Class[] classes) {
        Set<Class> classSet = new HashSet<>();

        for (Class clazz : classes) {
            boolean put = true;
            List<Class> classesToRemove = new ArrayList<>();
            for (Class classOfSet : classSet) {
                if (clazz.isAssignableFrom(classOfSet)) {
                    put = false;
                }
                if (classOfSet.isAssignableFrom(clazz) && !(classOfSet.equals(clazz))) {
                    classesToRemove.add(classOfSet);
                }
            }

            if (put) {
                classSet.add(clazz);
            }
            for (Class classToRemove : classesToRemove) {
                classSet.remove(classToRemove);
            }
        }
        return classSet;
    }
}
