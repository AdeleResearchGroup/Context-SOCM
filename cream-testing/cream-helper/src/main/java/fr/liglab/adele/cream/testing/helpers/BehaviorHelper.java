package fr.liglab.adele.cream.testing.helpers;

import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.runtime.internal.factories.BehaviorInstanceManager;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

/**
 * Created by aygalinc on 01/09/16.
 */
public class BehaviorHelper {

    private final IPOJOHelper ipojoHelper;

    public BehaviorHelper(IPOJOHelper service) {
        this.ipojoHelper = service;
    }

    public void stopBehavior(ComponentInstance instance,String behaviorId){
        ComponentInstance behaviorInstance = getBehavior(instance,behaviorId);
        behaviorInstance.stop();
    }


    public void disposeBehavior(ComponentInstance instance,String behaviorId){
        ComponentInstance behaviorInstance = getBehavior(instance,behaviorId);
        behaviorInstance.dispose();
    }

    /**
     * Start a behavior will put it in an invalid state because behaviorLifecycleHandler will be put in an invalid state after a start call.
     * A necessary call to valid behvior must be done in order to valid the behvior.
     *
     * @param instance
     * @param behaviorId
     */
    public void startBehavior(ComponentInstance instance, String behaviorId){
        ComponentInstance behaviorInstance = getBehavior(instance,behaviorId);
        behaviorInstance.start();

    }

    public void validBehavior(ComponentInstance instance, String behaviorId){
        ComponentInstance behaviorInstance = getBehavior(instance,behaviorId);
        BehaviorInstanceManager manager = (BehaviorInstanceManager) behaviorInstance;
        manager.getBehaviorLifeCycleHandler().startBehavior();
    }

    public void invalidBehavior(ComponentInstance instance, String behaviorId){
        ComponentInstance behaviorInstance = getBehavior(instance,behaviorId);
        BehaviorInstanceManager manager = (BehaviorInstanceManager) behaviorInstance;
        manager.getBehaviorLifeCycleHandler().stopBehavior();
    }


    public ComponentInstance getBehavior(ComponentInstance instance,String behaviorId){
        InstanceDescription instanceDescription = instance.getInstanceDescription();
        HandlerDescription managerDescription = instanceDescription.getHandlerDescription(HandlerReference.NAMESPACE+":"+HandlerReference.BEHAVIOR_MANAGER_HANDLER);
        Element element = managerDescription.getHandlerInfo();

        Element[] behaviorInstanceElement = element.getElements("instance");
        String instanceName = null;
        for (Element element1 : behaviorInstanceElement){
            Element[] handlerElement = element1.getElements("handler");
            for (Element element2:handlerElement){
                if ((HandlerReference.NAMESPACE+":"+HandlerReference.BEHAVIOR_LIFECYCLE_HANDLER).equals(element2.getAttribute("name"))){
                    String id = element2.getAttribute(BehaviorReference.BEHAVIOR_ID_CONFIG);
                    if (id.equals(behaviorId)){
                        instanceName = element1.getAttribute("name");
                        Architecture behaviorArchitecture = ipojoHelper.getArchitectureByName(instanceName);
                        InstanceDescription behaviorInstanceDescription = behaviorArchitecture.getInstanceDescription();
                        ComponentInstance behaviorInstance = behaviorInstanceDescription.getInstance();
                        return behaviorInstance;
                    }
                }
            }
        }
        return null;

    }
}
