package fr.liglab.adele.cream.annotations.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;

import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.Pojo;

import org.apache.felix.ipojo.handlers.providedservice.CreationStrategy;

import fr.liglab.adele.cream.annotations.internal.EntityInstance;



/**
 * Created by aygalinc on 10/06/16.
 */
public class ContextProvideStrategy extends CreationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(ContextProvideStrategy.class);
    private final Object lock = new Object();
    
    /**
     * The instance manager passed to the iPOJO ServiceFactory to manage instances.
     */
    private InstanceManager component;
    private List<Class<?>> 	publishedServices = new ArrayList<>();

    @Override
    public void onPublication(InstanceManager instance, String[] interfaces, Properties props) {
        
    	this.component = instance;

        ClassLoader componentLoader = component.getFactory().getBundleContext().getBundle().adapt(BundleWiring.class).getClassLoader();
        synchronized (lock) {
        	
        	publishedServices.clear();

            for (String serviceInterface : interfaces) {
                try {
                    Class<?> service = componentLoader.loadClass(serviceInterface);
                    if (service != null){
                    	publishedServices.add(service);
                    }
                } catch (ClassNotFoundException e) {
                    LOG.error("Unable to load interface " + serviceInterface +" , the proxy will not implement this interace",e);
                }
            }
        }
    }


	@Override
    public Object getService(Bundle bundle, @SuppressWarnings("rawtypes") ServiceRegistration registration) {

        ClassLoader componentLoader = component.getFactory().getBundleContext().getBundle().adapt(BundleWiring.class).getClassLoader();

        synchronized (lock) {

            Class<?> services []  = publishedServices.toArray( new Class[publishedServices.size() + 1]);
            services[services.length -1] = Pojo.class;
            try {
                return Proxy.newProxyInstance(componentLoader, services, ((EntityInstance) component).getDelegationHandler());
            } catch (java.lang.NoClassDefFoundError e) {
                LOG.warn("Import-package declaration in bundle that contains instance " + component.getInstanceName() + " isn't explicit enought to load class. Context Provide strategy cannot be used, singleton strategy used instead ! ", e);
                return component.getPojoObject();
            }
        }

    }

	@Override
    public void ungetService(Bundle bundle, @SuppressWarnings("rawtypes") ServiceRegistration registration, Object o) {
        //Do nothing on unget service
    }


    @Override
    public void onUnpublication() {
        //Do nothing on publication
    }

}
