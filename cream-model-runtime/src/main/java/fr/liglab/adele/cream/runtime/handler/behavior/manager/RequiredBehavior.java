package fr.liglab.adele.cream.runtime.handler.behavior.manager;

import fr.liglab.adele.cream.runtime.internal.factories.BehaviorFactory;
import fr.liglab.adele.cream.runtime.internal.factories.BehaviorInstanceManager;
import fr.liglab.adele.cream.utils.SuccessorStrategy;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by aygalinc on 02/06/16.
 */
public class RequiredBehavior implements InvocationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RequiredBehavior.class);

    private BehaviorFactory myFactory;

    private final String myName;

    private final String myBehaviorNameImpl;

    private BehaviorInstanceManager myManager;

    private final Hashtable myConfiguration = new Hashtable();

    public RequiredBehavior(String spec, String behaviorImpl, Dictionary config) {
        myName = spec;
        myBehaviorNameImpl = behaviorImpl;

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
    }

    public BehaviorFactory getFactory() {
        return myFactory;
    }

    public void setFactory(Factory factory) {
        if (factory instanceof BehaviorFactory){
            myFactory = (BehaviorFactory) factory;
        }
    }

    public String getSpecName() {
        return myName;
    }

    public String getImplName() {
        return myBehaviorNameImpl;
    }

    public synchronized BehaviorInstanceManager getManager() {
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
            myManager = (BehaviorInstanceManager) myFactory.createComponentInstance(myConfiguration,null);
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            LOG.error(UnacceptableConfiguration.class.getName(),unacceptableConfiguration);
        } catch (MissingHandlerException e) {
            LOG.error(MissingHandlerException.class.getName(),e);
        } catch (ConfigurationException e) {
            LOG.error(ConfigurationException.class.getName(),e);
        }
    }

    public synchronized boolean isOperationnal(){

        if (myManager != null ){
            return myManager.isOperationnal();
        }
        return false;
    }

    public synchronized void tryStartBehavior(){

        if (myManager != null ){
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
            myManager.getBehaviorLifeCycleHandler().stopBehavior();

        }
    }

    public synchronized void tryDispose(){
        if (myManager != null){
            myManager.dispose();
        }
    }

    public synchronized void getBehaviorDescription(Element elmentToAttach){
        if(myManager != null){
            InstanceDescription description = myManager.getInstanceDescription();

            Element behaviorDescription = description.getDescription();
            elmentToAttach.addElement(behaviorDescription);
        }
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (myManager != null && myManager.isStarted()){
            return myManager.getInvocationHandler().invoke(proxy,method,args);
        }
        return SuccessorStrategy.NO_FOUND_CODE;
    }

    public void registerBehaviorListener(ContextListener listener){
        myManager.registerBehaviorListener(listener);
    }

    public FieldInterceptor getBehaviorInterceptor(){
        return new BehaviorInjectedInterceptor();
    }

    private class BehaviorInjectedInterceptor implements FieldInterceptor{

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
