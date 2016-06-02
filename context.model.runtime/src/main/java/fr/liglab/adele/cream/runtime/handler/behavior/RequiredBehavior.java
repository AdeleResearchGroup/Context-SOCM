package fr.liglab.adele.cream.runtime.handler.behavior;

import fr.liglab.adele.cream.runtime.internal.factories.BehaviorFactory;
import fr.liglab.adele.cream.runtime.internal.factories.BehaviorManager;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

/**
 * Created by aygalinc on 02/06/16.
 */
public class RequiredBehavior {

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

    public BehaviorManager getManager() {
        return myManager;
    }

    public void unRef() {
        myFactory = null;
        myManager = null;
    }

    public void addManager() {
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

    public void tryStartBehavior(){

        if (myManager != null){
           if (firstStart) {
               firstStart=false;
               myManager.start();
           }else {
               myManager.setState(ComponentInstance.VALID);
           }
        }
    }

    public void tryInvalid(){
        if (myManager != null){
            myManager.setState(ComponentInstance.INVALID);
        }
    }

    public void tryDispose(){
        if (myManager != null){
            myManager.setState(ComponentInstance.DISPOSED);
        }
    }

    public void getBehaviorDescription(Element elmentToAttach){
        if(myManager != null){
            InstanceDescription description = myManager.getInstanceDescription();

            Element behaviorDescription = description.getDescription();
            elmentToAttach.addElement(behaviorDescription);
        }
    }
}
