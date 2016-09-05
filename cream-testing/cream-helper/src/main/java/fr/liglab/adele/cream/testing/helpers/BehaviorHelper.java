package fr.liglab.adele.cream.testing.helpers;

import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

/**
 * Created by aygalinc on 01/09/16.
 */
public class BehaviorHelper {

    private final OSGiHelper osgiHelper;
    private final IPOJOHelper ipojoHelper;

    public BehaviorHelper(OSGiHelper osgi, IPOJOHelper service) {
        this.osgiHelper = osgi;
        this.ipojoHelper = service;
    }

    public void stopBehavior(ComponentInstance instance,String behaviorId){
        ComponentInstance behaviorInstance = getBehavior(instance,behaviorId);
        behaviorInstance.stop();
    }


    public void dispose(ComponentInstance instance,String behaviorId){
        ComponentInstance behaviorInstance = getBehavior(instance,behaviorId);
        behaviorInstance.dispose();
    }

    public void start(ComponentInstance instance,String behaviorId){
        ComponentInstance behaviorInstance = getBehavior(instance,behaviorId);
        behaviorInstance.start();
    }

    public ComponentInstance getBehavior(ComponentInstance instance,String behaviorId){
        InstanceDescription instanceDescription = instance.getInstanceDescription();
        HandlerDescription managerDescription = instanceDescription.getHandlerDescription(HandlerReference.NAMESPACE+":"+HandlerReference.BEHAVIOR_MANAGER_HANDLER);
        Element element = managerDescription.getHandlerInfo();

        String instanceName = null;
        for (Element singleElement: element.getElements()){
            String id = singleElement.getAttribute(BehaviorReference.BEHAVIOR_ID_CONFIG);
            if (id != null && id.equals(behaviorId)){
                instanceName = singleElement.getAttribute("name");
            }
        }
        return ipojoHelper.getInstanceByName(instanceName);
    }
}
