package fr.liglab.adele.cream.event.handler.impl;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.dependency.interceptors.TransformedServiceReference;
import org.apache.felix.ipojo.handlers.dependency.Dependency;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.util.Callback;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aygalinc on 09/06/16.
 */
public class ContextUpdateElement {

    private static final Logger LOG = LoggerFactory.getLogger(ContextUpdateElement.class);

    private final Callback myCallback;

    private final String mySpecName;

    private final String myPropertyName;
    private final Object myLock = new Object();
    private final BundleContext myContext;
    private final InstanceManager myManager;
    private Object myOldValue;

    public ContextUpdateElement(String specName, String stateElementName, MethodMetadata metadata, InstanceManager manager) {
        mySpecName = specName;
        myManager = manager;
        String simpleClassName = specName.substring(specName.lastIndexOf('.') + 1);
        myPropertyName = ContextEntity.State.id(simpleClassName, stateElementName);
        myContext = manager.getContext();
        myCallback = new Callback(metadata, manager);
    }

    private static void getAllInterface(Class clazz, List<String> returnList) {
        if (clazz.isInterface()) {
            returnList.add(clazz.getName());
        }
        for (Class interfaz : clazz.getInterfaces()) {
            getAllInterface(interfaz, returnList);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Dependency) {
            Dependency dependency = (Dependency) obj;

            List<String> specToCompare = new ArrayList<>();

            getAllInterface(dependency.getSpecification(), specToCompare);

            for (String spec : specToCompare) {
                if (mySpecName.equals(spec)) {
                    return true;
                }
            }
            return false;
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return mySpecName.hashCode() + myPropertyName.hashCode();
    }

    public void updateIfNecessary(TransformedServiceReference ref) {
        if (myManager.getState() != ComponentInstance.VALID) {
            return;
        }
        synchronized (myLock) {
            Object prop = ref.getProperty(myPropertyName);
            if (myOldValue == null) {
                myOldValue = prop;
            } else if (myOldValue.equals(prop)) {
                return;
            } else {
                Object oldValue = myOldValue;
                myOldValue = prop;
                Object serviceObject = myContext.getService(ref);
                Object[] args = {serviceObject, myOldValue, oldValue};
                try {
                    myCallback.call(args);
                } catch (NoSuchMethodException e) {
                    LOG.error("Error occurs during method invocation , ", e);
                } catch (IllegalAccessException e) {
                    LOG.error("Error occurs during method invocation , ", e);
                } catch (InvocationTargetException e) {
                    LOG.error("Error occurs during method invocation , ", e);
                }
            }
        }
    }

    public void clearValue() {
        synchronized (myLock) {
            myOldValue = null;
        }
    }
}
