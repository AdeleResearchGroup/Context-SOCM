package fr.liglab.adele.cream.utils;

import org.apache.felix.ipojo.InstanceManager;

import java.lang.reflect.Constructor;

/**
 * Created by aygalinc on 09/09/16.
 */
public class CreamProxyFactory extends ClassLoader {

    /**
     * Handler classloader, used to load the temporal dependency class.
     */
    private final ClassLoader m_InstanceManagerClassLoader;

    private final InstanceManager manager;

    /**
     * Creates the proxy classloader.
     *
     * @param parent the handler classloader.
     */
    public CreamProxyFactory(ClassLoader parent, InstanceManager manager) {
        super(manager.getFactory().getBundleClassLoader());
        m_InstanceManagerClassLoader = parent;
        this.manager = manager;
    }

    /**
     * Loads a proxy class generated for the given (interface) class.
     *
     * @param clazz the service specification to proxy
     * @return the Class object of the proxy.
     */
    protected Class getProxyClass(Class clazz) {
        byte[] clz = CreamProxyGenerator.dump(clazz,manager.getClazz(),manager.getInstanceName()); // Generate the proxy.
        // Turn around the VM changes (FELIX-2716) about java.* classes.
        String cn = clazz.getName();
        if (cn.startsWith("java.")) {
            cn = "$" + cn;
        }
        return defineClass(cn + "$$Proxy"+manager.getInstanceName().hashCode(), clz, 0, clz.length);
    }

    public Object getProxy(Class spec) {
        try {
            Class clazz = getProxyClass(spec);
            Constructor constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (Throwable e) {

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
    public Class loadClass(String name) throws ClassNotFoundException {
        try {
            return manager.getContext().getBundle().loadClass(name);
        } catch (ClassNotFoundException e) {
            return m_InstanceManagerClassLoader.loadClass(name);
        }
    }
}

