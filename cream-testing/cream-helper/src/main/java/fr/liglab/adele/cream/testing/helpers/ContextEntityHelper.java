package fr.liglab.adele.cream.testing.helpers;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

import java.util.Hashtable;
import java.util.Map;

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

    public void createContextEntity(String contextEntityType, String contextEntityId, Map<String,Object> contextEntityInitParameters) throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        createContextEntity(contextEntityType,contextEntityId,contextEntityInitParameters,null);
    }

    public void createContextEntity(String contextEntityType, String contextEntityId, Map<String,Object> contextEntityInitParameters,Map<String,Object> pojoParameters) throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        Factory contextEntityFactory = getContextEntityFactory(contextEntityType);
        Hashtable param = new Hashtable();

        param.put("context.entity.id",contextEntityId);

        if (contextEntityInitParameters != null) {
            param.putAll(contextEntityInitParameters);
        }
        if (pojoParameters != null){
            param.putAll(pojoParameters);
        }

        contextEntityFactory.createComponentInstance(param);

    }
}
