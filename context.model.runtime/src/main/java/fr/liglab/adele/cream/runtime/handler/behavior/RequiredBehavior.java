package fr.liglab.adele.cream.runtime.handler.behavior;

import fr.liglab.adele.cream.runtime.internal.factories.BehaviorFactory;
import fr.liglab.adele.cream.runtime.internal.factories.BehaviorManager;
import fr.liglab.adele.cream.runtime.internal.utils.SuccessorStrategy;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Hashtable;

/**
 * Created by aygalinc on 02/06/16.
 */
public class RequiredBehavior implements InvocationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RequiredBehavior.class);

    private BehaviorFactory myFactory;

    private final String myName;

    private final String myBehaviorNameImpl;

    private BehaviorManager myManager;

    private volatile boolean firstStart = true;

    public RequiredBehavior(String spec, String behaviorImpl) {
        myName = spec;
        myBehaviorNameImpl = behaviorImpl;
    }

    public int hashCode() {
        return super.hashCode();
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

    public synchronized BehaviorManager getManager() {
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
        Hashtable prop = new Hashtable();
        try {
            myManager = (BehaviorManager) myFactory.createComponentInstance(prop,null);
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            LOG.error(unacceptableConfiguration.toString());
        } catch (MissingHandlerException e) {
            LOG.error(e.toString());
        } catch (ConfigurationException e) {
            LOG.error(e.toString());
        }
    }

    public synchronized void tryStartBehavior(){

        if (myManager != null){
            if (firstStart) {
                firstStart=false;
                myManager.start();
            }else {
                myManager.setState(ComponentInstance.VALID);
            }
        }
    }

    public synchronized void tryInvalid(){
        if (myManager != null){
            myManager.setState(ComponentInstance.INVALID);
        }
    }

    public synchronized void tryDispose(){
        if (myManager != null){
            myManager.setState(ComponentInstance.DISPOSED);
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
        if (myManager != null){
            return myManager.getInvocationHandler().invoke(proxy,method,args);
        }
        return SuccessorStrategy.NO_FOUND_CODE;
    }
}
