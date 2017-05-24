package fr.liglab.adele.cream.runtime.handler.functional.extension.tracker;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.internal.FunctionalExtensionReference;
import fr.liglab.adele.cream.runtime.handler.functional.extension.lifecycle.FunctionalExtensionStateListener;
import fr.liglab.adele.cream.runtime.internal.factories.FunctionalExtensionFactory;
import fr.liglab.adele.cream.runtime.internal.factories.FunctionalExtensionInstanceManager;
import fr.liglab.adele.cream.utils.SuccessorStrategy;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedService;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.ParseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by aygalinc on 02/06/16.
 */
public class RequiredFunctionalExtension implements InvocationHandler, FunctionalExtensionStateListener, ContextSource {

    private static final String EXTENSION_CONTROLLER_FIELD = "extension.controller.";

    private static final Logger LOG = LoggerFactory.getLogger(RequiredFunctionalExtension.class);

    private final String myStringSpecifications;

    private final String[] mySpecifications;

    private String myExtensionNameImpl;

    private final Hashtable myConfiguration = new Hashtable();

    private final FunctionalExtensionTrackerHandler parent;

    private final ProvidedServiceHandler myProvideServiceHandler;

    private final String myId;

    private final Object lock = new Object();

    private FunctionalExtensionInstanceManager myManager;

    private FunctionalExtensionFactory myCurrentFactory;

    private final Map<String,FunctionalExtensionFactory> myAlternativeFactoryConfiguration = new ConcurrentHashMap<>();

    private ContextSource extensionContextSource;

    private ContextListener contextListener;

    private String[] propertiesToListen;

    private final boolean mandatory;

    public RequiredFunctionalExtension(String id, String specs, String behaviorImpl, Dictionary config, FunctionalExtensionTrackerHandler parent, ProvidedServiceHandler providedServiceHandler,boolean mandatory) {
        myStringSpecifications = specs;
        mySpecifications = ParseUtils.parseArrays(specs);
        myExtensionNameImpl = behaviorImpl;
        myId = id;
        myProvideServiceHandler = providedServiceHandler;

        this.mandatory = mandatory;

        /**
         * Extract Dictionnary properrties
         */
        Enumeration enumeration = config.keys();
        config.size();
        while (enumeration.hasMoreElements()) {
            Object key = enumeration.nextElement();
            Object value = config.get(key);
            if (!("instance.name".equals(key))) {
                myConfiguration.put(key, value);
            }
        }
        myConfiguration.put(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_ID_CONFIG.toString(), id);
        this.parent = parent;

    }


    public void setProvidedService(ProvidedService providedService) {
        for (String spec : mySpecifications) {
            providedService.setController(EXTENSION_CONTROLLER_FIELD + myId + spec, false, spec);
        }
    }

    public FunctionalExtensionFactory getFactory() {
        return myCurrentFactory;
    }

    public boolean tryToAddFactory(Factory factory,List<String> factorySpecifications,String factoryImplementation) {
        if (! (factory instanceof FunctionalExtensionFactory)) {
            return false;
        }

        FunctionalExtensionFactory functionalExtensionFactory = (FunctionalExtensionFactory) factory;

        if(! specificationMatch(factorySpecifications)){
            return false;
        }

        myAlternativeFactoryConfiguration.put(factoryImplementation,functionalExtensionFactory);

        if (! implementationMatch(factoryImplementation)){
            return false;
        }

        myCurrentFactory = functionalExtensionFactory;

        return true;
    }

    protected boolean implementationMatch( String factoryImplementation) {

        return getImplName().equalsIgnoreCase(factoryImplementation);
    }

    protected boolean specificationMatch( List<String> factorySpecifications) {

        boolean specMatch = true;



        for (String spec : getSpecName()) {
            if (!factorySpecifications.contains(spec)) {
                specMatch = false;
            }
        }
        return specMatch;
    }

    public boolean isMandatory() {
        return mandatory;
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

    public void factoryDeparture(Factory factory,String factoryImplementation) {

        if (factory.equals(myCurrentFactory)){
            myCurrentFactory = null;
        }

        myAlternativeFactoryConfiguration.remove(factoryImplementation);

        myManager = null;
    }

    public synchronized void addManager() {
        if (myCurrentFactory == null) {
            LOG.warn("Try to add new functional extension manager but the factory of the functional extension is null");
            return;
        }else if (myManager != null ) {
            LOG.warn("Try to add new functional extension manager but the extension currently have one");
            return;
        }

        try {
            synchronized (lock) {
                myManager = (FunctionalExtensionInstanceManager) myCurrentFactory.createComponentInstance(myConfiguration, null);
                myManager.getBehaviorLifeCycleHandler().registerBehaviorListener(this);
                myManager.registerContextListenerToExtensionEntityHandler(parent.getBehaviorContextListener());
                extensionContextSource = myManager.getExtensionContextSource();
                if (contextListener != null) {
                    extensionContextSource.registerContextListener(contextListener, propertiesToListen);
                }
            }
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            LOG.error(UnacceptableConfiguration.class.getName(), unacceptableConfiguration);
        } catch (MissingHandlerException e) {
            LOG.error(MissingHandlerException.class.getName(), e);
        } catch (ConfigurationException e) {
            LOG.error(ConfigurationException.class.getName(), e);
        }
    }

    public synchronized void tryStartExtension() {

        if (myManager != null) {
            Set<String> properties = myManager.getBehaviorLifeCycleHandler().getPropertiesToListen();
            parent.registerContextEntityContextListener(myManager.getBehaviorLifeCycleHandler(), properties.toArray(new String[properties.size()]));

            if (!myManager.isStarted()) {
                myManager.start();
                myManager.getBehaviorLifeCycleHandler().startBehavior();
            } else {
                myManager.getBehaviorLifeCycleHandler().startBehavior();
            }
        }
    }

    public synchronized void tryInvalid() {
        if (myManager != null && myManager.isStarted()) {
            for (String spec : mySpecifications) {
                myProvideServiceHandler.onSet(null, EXTENSION_CONTROLLER_FIELD + myId + spec, false);
            }
            myManager.getBehaviorLifeCycleHandler().stopBehavior();
            parent.unregisterContextEntityContextListener(myManager.getBehaviorLifeCycleHandler());
        }
    }

    public synchronized void tryDispose() {
        if (myManager != null) {
            for (String spec : mySpecifications) {
                myProvideServiceHandler.onSet(null, EXTENSION_CONTROLLER_FIELD + myId + spec, false);
            }
            parent.unregisterContextEntityContextListener(myManager.getBehaviorLifeCycleHandler());
            myManager.getBehaviorLifeCycleHandler().unregisterBehaviorListener(parent);
            myManager.unregisterContextListenerToExtensionEntityHandler(parent.getBehaviorContextListener());
            myManager.dispose();

            //unref Manager
            myManager = null;
        }
    }

    public synchronized void getExtensionDescription(Element elementToAttach) {
        Element extensionElement = new Element(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_INDIVIDUAL_ELEMENT_NAME.toString(),"");
        extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.ID_ATTRIBUTE_NAME.toString(),myId));
        extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_MANAGED_SPECS_CONFIG.toString(),myStringSpecifications));
        extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_MANDATORY_ATTRIBUTE_NAME.toString(),String.valueOf(mandatory)));
        extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString(),myExtensionNameImpl));
        extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_ALTERNATIVE_CONFIGURATION.toString(), myAlternativeFactoryConfiguration.keySet().stream().collect(Collectors.joining(",", "{", "}"))));


        if (myManager != null) {
            extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_IS_INSTANTIATE.toString(),"true"));

            InstanceDescription description = myManager.getInstanceDescription();

            Element behaviorDescription = description.getDescription();
            extensionElement.addElement(behaviorDescription);
            elementToAttach.addElement(extensionElement);
        }else {
            extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_IS_INSTANTIATE.toString(),"false"));
        }
    }

    public synchronized boolean isValid() {
        if (myManager != null) {
            return (myManager.getState() == ComponentInstance.VALID);
        }
        return false;
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (myManager != null && myManager.isStarted()) {
            return myManager.getInvocationHandler().invoke(proxy, method, args);
        }
        return SuccessorStrategy.NO_FOUND_CODE;
    }

    public FieldInterceptor getExtensionInterceptor() {
        return new InjectedExtensionInterceptor();
    }

    @Override
    public void functionalExtensionStateChange(int state) {
        if (state == ComponentInstance.VALID) {
            for (String spec : mySpecifications) {
                myProvideServiceHandler.onSet(null, EXTENSION_CONTROLLER_FIELD + myId + spec, true);
            }
        } else if (state == ComponentInstance.INVALID) {
            for (String spec : mySpecifications) {
                myProvideServiceHandler.onSet(null, EXTENSION_CONTROLLER_FIELD + myId + spec, false);
            }
        }
        parent.functionalExtensionStateChange(state);
    }

    @Override
    public Object getProperty(String property) {
        if (myManager != null && myManager.isStarted() && extensionContextSource != null) {
            return extensionContextSource.getProperty(property);
        }
        return null;
    }

    @Override
    public Dictionary getContext() {
        if (myManager != null && myManager.isStarted() && extensionContextSource != null) {
            return extensionContextSource.getContext();
        }
        return new Hashtable<>();
    }

    @Override
    public void registerContextListener(ContextListener listener, String[] properties) {
        synchronized (lock) {
            contextListener = listener;
            propertiesToListen = properties;
            if (extensionContextSource != null) {
                extensionContextSource.registerContextListener(listener, properties);
            }
        }
    }

    @Override
    public void unregisterContextListener(ContextListener listener) {
        synchronized (lock) {
            if (extensionContextSource != null) {
                extensionContextSource.unregisterContextListener(listener);
                if (listener != null && listener.equals(contextListener)) {
                    contextListener = null;
                }
            }
        }
    }

    private class InjectedExtensionInterceptor implements FieldInterceptor {

        @Override
        public void onSet(Object pojo, String fieldName, Object value) {
//On set method do nothing
        }

        @Override
        public Object onGet(Object pojo, String fieldName, Object value) {
            return myManager.getPojoObject();
        }
    }

    public void propagateReconfigure(Dictionary configuration){
        if (myManager != null) {
            myManager.reconfigure(configuration);
        }
    }

    public synchronized void tryFunctionalExtensionReconfiguration(String newFunctionalExtensionImplementation){
        if ( newFunctionalExtensionImplementation == null){
            LOG.warn("The reconfiguration cannot be done because functional implementation class is null" );
            return;
        }
        myExtensionNameImpl = newFunctionalExtensionImplementation;
        tryDispose();

        myCurrentFactory = myAlternativeFactoryConfiguration.get(newFunctionalExtensionImplementation);

        if (myCurrentFactory == null){
            LOG.warn("The reconfiguration cannot be done because no alternative configuration correspon to the class" + newFunctionalExtensionImplementation);
            return;
        }

        addManager();
    }

}
