package fr.liglab.adele.cream.utils;

import org.apache.felix.ipojo.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Created by aygalinc on 09/09/16.
 */
public class CreamProxyFactory extends ClassLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CreamProxyFactory.class);

    /**
     * Handler classloader, used to load the temporal dependency class.
     */
    private final ClassLoader myInstanceManagerClassLoader;

    private final InstanceManager manager;

    /**
     * Creates the proxy classloader.
     *
     * @param parent the handler classloader.
     */
    public CreamProxyFactory(ClassLoader parent, InstanceManager manager) {
        super(manager.getFactory().getBundleClassLoader());
        myInstanceManagerClassLoader = parent;
        this.manager = manager;
    }

    /**
     * Loads a proxy class generated for the given (interface) class.
     *
     * @param clazz the service specification to proxy
     * @return the Class object of the proxy.
     */
    protected Class<?> getProxyClass(Class<?> clazz) {
        byte[] clz = CreamProxyGenerator.dump(clazz, manager.getInstanceName()); // Generate the proxy.
        // Turn around the VM changes (FELIX-2716) about java.* classes.
        String cn = clazz.getName();
        if (cn.startsWith("java.")) {
            cn = "$" + cn;
        }
        return defineClass(cn + "$$Proxy" + manager.getInstanceName().hashCode(), clz, 0, clz.length);
    }

    public Object getProxy(Class<?> spec) {
        try {
            Class<?> clazz = getProxyClass(spec);
            Constructor<?> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            LOG.error("error during proxy generation", e);
            return null;
        }
    }

    /**
     * Loads the given class.
     * This method uses the classloader of the specification class
     * or the handler class loader.
     *
     * @param name the class name
     * @return the class object
     * @throws ClassNotFoundException if the class is not found by the two classloaders.
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return manager.getContext().getBundle().loadClass(name);
        } catch (ClassNotFoundException e) {
            LOG.debug("Classloading delegation ", e);
            return myInstanceManagerClassLoader.loadClass(name);
        }
    }
}

