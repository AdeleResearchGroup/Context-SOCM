package fr.adele.cream.testing.helpers;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.HandlerFactory;
import org.apache.felix.ipojo.ServiceContext;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

/**
 * Created by aygalinc on 25/07/16.
 */
public class ContextEntityHelper {

    private final OSGiHelper osgiHelper;
    private final IPOJOHelper ipojoHelper;

    public ContextEntityHelper(OSGiHelper osgi, IPOJOHelper service) {
        this.osgiHelper = osgi;
        this.ipojoHelper = service;
    }

    public Factory getContextEntityFactory(String factoryName) {
        return this.getContextEntityFactory(factoryName, 0L);
    }

    public Factory getContextEntityFactory(String factoryName, long timeout) {
        return this.getContextEntityFactory(factoryName, timeout, true);
    }

    public Factory getContextEntityFactory(String factoryName, long timeout, boolean fail) {
        return (Factory)this.osgiHelper.waitForService(Factory.class, "(factory.name=" + factoryName + ")", timeout, fail);
    }
}
