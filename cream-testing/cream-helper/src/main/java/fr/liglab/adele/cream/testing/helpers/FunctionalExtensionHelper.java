package fr.liglab.adele.cream.testing.helpers;

import fr.liglab.adele.cream.annotations.internal.FunctionalExtensionReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.runtime.handler.functional.extension.lifecycle.FunctionalExtensionLifecyleHandler;
import fr.liglab.adele.cream.runtime.internal.factories.FunctionalExtensionInstanceManager;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;

/**
 * Created by aygalinc on 01/09/16.
 */
public class FunctionalExtensionHelper {

    private final IPOJOHelper ipojoHelper;

    public FunctionalExtensionHelper(IPOJOHelper service) {
        this.ipojoHelper = service;
    }

    public void stopFunctionalExtension(ComponentInstance instance, String behaviorId) {
        ComponentInstance behaviorInstance = getFunctionalExtension(instance, behaviorId);
        behaviorInstance.stop();
    }


    public void disposeFunctionalExtension(ComponentInstance instance, String functionalExtensionId) {
        ComponentInstance behaviorInstance = getFunctionalExtension(instance, functionalExtensionId);
        behaviorInstance.dispose();
    }

    /**
     * Start a functional Extension will put it in an invalid state because FunctionalExtensionLifeCycleHandler will be put in an invalid state after a start call.
     * A necessary call to valid behvior must be done in order to valid the functional extension.
     *
     * @param instance
     * @param functionalExtensionId
     */
    public void startFunctionalExtension(ComponentInstance instance, String functionalExtensionId) {
        ComponentInstance behaviorInstance = getFunctionalExtension(instance, functionalExtensionId);
        behaviorInstance.start();

    }

    public void validFunctionalExtension(ComponentInstance instance, String functionalExtensionId) {
        ComponentInstance behaviorInstance = getFunctionalExtension(instance, functionalExtensionId);
        FunctionalExtensionInstanceManager manager = (FunctionalExtensionInstanceManager) behaviorInstance;
        FunctionalExtensionLifecyleHandler.forInstance(manager).setValidity(true);
    }

    public void invalidFunctionalExtension(ComponentInstance instance, String functionalExtensionId) {
        ComponentInstance functionalExtensionInstance = getFunctionalExtension(instance, functionalExtensionId);
        FunctionalExtensionInstanceManager manager = (FunctionalExtensionInstanceManager) functionalExtensionInstance;
        FunctionalExtensionLifecyleHandler.forInstance(manager).setValidity(false);
    }


    public ComponentInstance getFunctionalExtension(ComponentInstance instance, String behaviorId) {
        InstanceDescription instanceDescription = instance.getInstanceDescription();
        HandlerDescription managerDescription = instanceDescription.getHandlerDescription(HandlerReference.NAMESPACE + ":" + HandlerReference.FUNCTIONAL_EXTENSION_TRACKER_HANDLER);
        Element element = managerDescription.getHandlerInfo();

        Element[] functionalExtensionElements = element.getElements(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_INDIVIDUAL_ELEMENT_NAME.toString());

        for (Element functionalExtensionElement : functionalExtensionElements) {
            String id = functionalExtensionElement.getAttribute(FunctionalExtensionReference.ID_ATTRIBUTE_NAME.toString());

            if(!behaviorId.equals(id)) {
                continue;
            }

            Element[] instanceElements = functionalExtensionElement.getElements("instance");
            for (Element instanceElement : instanceElements) {
                String instanceName = instanceElement.getAttribute("name");
                Architecture behaviorArchitecture = ipojoHelper.getArchitectureByName(instanceName);
                InstanceDescription behaviorInstanceDescription = behaviorArchitecture.getInstanceDescription();
                return behaviorInstanceDescription.getInstance();

            }
        }

        return null;
    }
}
