package fr.liglab.adele.cream.runtime.handler.functional.extension.tracker;

import fr.liglab.adele.cream.annotations.internal.FunctionalExtensionReference;
import fr.liglab.adele.cream.runtime.handler.functional.extension.lifecycle.FunctionalExtensionStateListener;
import fr.liglab.adele.cream.runtime.internal.factories.FunctionalExtensionFactory;
import fr.liglab.adele.cream.runtime.internal.factories.FunctionalExtensionInstanceManager;
import fr.liglab.adele.cream.utils.SuccessorStrategy;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedService;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by aygalinc on 02/06/16.
 */
public class RequiredFunctionalExtension implements InvocationHandler,FunctionalExtensionStateListener,ContextSource{

    private static final String EXTENSION_CONTROLLER_FIELD = "extension.controller.";

    private static final Logger LOG = LoggerFactory.getLogger(RequiredFunctionalExtension.class);

    private final String[] mySpecifications;

    private final String myExtensionNameImpl;

    private final Hashtable myConfiguration = new Hashtable();

    private final BehaviorTrackerHandler parent;

    private final ProvidedServiceHandler myProvideServiceHandler;

    private final String myId ;

    private FunctionalExtensionInstanceManager myManager;

    private FunctionalExtensionFactory myFactory;

    private ContextSource extensionContextSource;

    private ContextListener contextListener;

    private String[] propertiesToListen;

    private final Object lock = new Object();

    public RequiredFunctionalExtension(String id, String[] spec, String behaviorImpl, Dictionary config, BehaviorTrackerHandler parent, ProvidedServiceHandler providedServiceHandler) {
        mySpecifications = spec;
        myExtensionNameImpl = behaviorImpl;
        myId = id;
        myProvideServiceHandler = providedServiceHandler;

        /**
         * Extract Dictionnary properrties
         */
        Enumeration enumeration = config.keys();
        config.size();
        while (enumeration.hasMoreElements()){
            Object key = enumeration.nextElement();
            Object value = config.get(key);
            if (!("instance.name".equals(key))){
                myConfiguration.put(key,value);
            }
        }
        myConfiguration.put(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_ID_CONFIG.toString(),id);
        this.parent = parent;

    }


    public void setProvidedService(ProvidedService providedService){
        for (String spec : mySpecifications) {
            providedService.setController(EXTENSION_CONTROLLER_FIELD + myId+spec, false, spec);
        }
    }

    public FunctionalExtensionFactory getFactory() {
        return myFactory;
    }

    public void setFactory(Factory factory) {
        if (factory instanceof FunctionalExtensionFactory){
            myFactory = (FunctionalExtensionFactory) factory;
        }
    }

    public String[] getSpecName() {
        return mySpecifications;
    }

    public String getImplName() {
        return myExtensionNameImpl;
    }

    public synchronized FunctionalExtensionInstanceManager getManager() {
        return myManager;
    }

    public void unRef() {
        myFactory = null;
        myManager = null;
    }

    public synchronized void addManager() {
        if (myManager != null || myFactory == null){
            return;
        }

        try {
            synchronized (lock) {
                myManager = (FunctionalExtensionInstanceManager) myFactory.createComponentInstance(myConfiguration, null);
                myManager.getBehaviorLifeCycleHandler().registerBehaviorListener(this);
                myManager.registerContextListenerToExtensionEntityHandler(parent.getBehaviorContextListener());
                extensionContextSource = myManager.getExtensionContextSource();
                if (contextListener != null){
                    extensionContextSource.registerContextListener(contextListener,propertiesToListen);
                }
            }
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            LOG.error(UnacceptableConfiguration.class.getName(),unacceptableConfiguration);
        } catch (MissingHandlerException e) {
            LOG.error(MissingHandlerException.class.getName(),e);
        } catch (ConfigurationException e) {
            LOG.error(ConfigurationException.class.getName(),e);
        }
    }

    public synchronized void tryStartExtension(){

        if (myManager != null ){
            Set<String> properties =  myManager.getBehaviorLifeCycleHandler().getPropertiesToListen();
            parent.registerContextEntityContextListener(myManager.getBehaviorLifeCycleHandler() ,properties.toArray(new String[properties.size()]));

            if (!myManager.isStarted()) {
                myManager.start();
                myManager.getBehaviorLifeCycleHandler().startBehavior();
            }else {
                myManager.getBehaviorLifeCycleHandler().startBehavior();
            }
        }
    }

    public synchronized void tryInvalid(){
        if (myManager != null && myManager.isStarted() ){
            for (String spec : mySpecifications){
                myProvideServiceHandler.onSet(null, EXTENSION_CONTROLLER_FIELD +myId+spec,false);
            }
            myManager.getBehaviorLifeCycleHandler().stopBehavior();
            parent.unregisterContextEntityContextListener(myManager.getBehaviorLifeCycleHandler());
        }
    }

    public synchronized void tryDispose(){
        if (myManager != null){
            for (String spec : mySpecifications){
                myProvideServiceHandler.onSet(null, EXTENSION_CONTROLLER_FIELD +myId+spec,false);
            }
            parent.unregisterContextEntityContextListener(myManager.getBehaviorLifeCycleHandler());
            myManager.getBehaviorLifeCycleHandler().unregisterBehaviorListener(parent);
            myManager.unregisterContextListenerToExtensionEntityHandler(parent.getBehaviorContextListener());
            myManager.dispose();
        }
    }

    public synchronized void getExtensionDescription(Element elmentToAttach){
        if(myManager != null){
            InstanceDescription description = myManager.getInstanceDescription();

            Element behaviorDescription = description.getDescription();
            elmentToAttach.addElement(behaviorDescription);
        }
    }

    public synchronized boolean isValid(){
        if (myManager != null){
            return (myManager.getState() == ComponentInstance.VALID);
        }
        return false;
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (myManager != null && myManager.isStarted()){
            return myManager.getInvocationHandler().invoke(proxy,method,args);
        }
        return SuccessorStrategy.NO_FOUND_CODE;
    }

    public FieldInterceptor getExtensionInterceptor(){
        return new InjectedExtensionInterceptor();
    }

    @Override
    public void functionalExtensionStateChange(int state, String id) {
        if (state == ComponentInstance.VALID){
            for (String spec : mySpecifications){
                myProvideServiceHandler.onSet(null, EXTENSION_CONTROLLER_FIELD +myId+spec,true);
            }
        }
        else if (state == ComponentInstance.INVALID){
            for (String spec : mySpecifications){
                myProvideServiceHandler.onSet(null, EXTENSION_CONTROLLER_FIELD +myId+spec,false);
            }
        }
        parent.functionalExtensionStateChange(state,id);
    }

    @Override
    public Object getProperty(String property) {
        if (myManager != null && myManager.isStarted()  && extensionContextSource != null){
            return extensionContextSource.getProperty(property);
        }
        return null;
    }

    @Override
    public Dictionary getContext() {
        if (myManager != null && myManager.isStarted() && extensionContextSource != null){
            return extensionContextSource.getContext();
        }
        return new Hashtable<>();
    }

    @Override
    public void registerContextListener(ContextListener listener, String[] properties) {
        synchronized (lock) {
            contextListener = listener;
            propertiesToListen = properties;
            if (extensionContextSource != null){
                extensionContextSource.registerContextListener(listener,properties);
            }
        }
    }

    @Override
    public void unregisterContextListener(ContextListener listener) {
        synchronized (lock) {
            if (extensionContextSource != null){
                extensionContextSource.unregisterContextListener(listener);
                if (listener != null && listener.equals(contextListener)){
                    contextListener = null;
                }
            }
        }
    }

    private class InjectedExtensionInterceptor implements FieldInterceptor{

        @Override
        public void onSet(Object pojo, String fieldName, Object value) {
//On set method do nothing
        }

        @Override
        public Object onGet(Object pojo, String fieldName, Object value) {
            return myManager.getPojoObject();
        }
    }

}
