package fr.liglab.adele.cream.event.handler.implem;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.dependency.interceptors.TransformedServiceReference;
import org.apache.felix.ipojo.handlers.dependency.Dependency;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.util.Callback;
import org.osgi.framework.BundleContext;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by aygalinc on 09/06/16.
 */
public class ContextUpdateElement {

    private final Callback myCallback;

    private final String mySpecName;

    private final String myStateElementName;

    private final String myPropertyName;

    private Object myOldValue;

    private final Object myLock = new Object();

    private final BundleContext myContext;

    private final InstanceManager myManager;

    public ContextUpdateElement(String specName, String stateElementName, MethodMetadata metadata, InstanceManager manager){
        mySpecName = specName;
        myStateElementName =stateElementName;
        myManager = manager;
        String simpleClassName = specName.substring(specName.lastIndexOf('.') + 1);
        myPropertyName = ContextEntity.State.ID(simpleClassName,stateElementName);
        myContext = manager.getContext();
        myCallback = new Callback(metadata,manager);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Dependency){
            Dependency dependency = (Dependency) obj;
            /**
             * TODO : interface hierarchy must be introspect, in order to check if the CS affiliate to the state is a part of the interface Hierarchy
             */
            String dependencySpec = dependency.getSpecification().getName();
            if (mySpecName.equals(dependencySpec)){
                return true;
            }
        }

        return super.equals(obj);
    }

    public void updateIfNecessary(TransformedServiceReference ref){
        if (myManager.getState() != ComponentInstance.VALID){
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
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void clearValue(){
        synchronized (myLock){
            myOldValue = null;
        }
    }
}
