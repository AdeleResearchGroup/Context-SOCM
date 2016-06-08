package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.util.Logger;
import org.osgi.framework.BundleContext;

import java.util.Dictionary;
import java.util.List;

/**
 * Created by aygalinc on 31/05/16.
 */
public class BehaviorFactory extends ComponentFactory {

    private final String mySpec;

    private final String myImplem;

    public BehaviorFactory(BundleContext context, Element element) throws ConfigurationException {
        super(context, element);
        // Get the type
        String spec = element.getAttribute(BehaviorReference.SPEC_ATTR_NAME);
        if (spec != null) {
            mySpec = spec;
        } else {
            throw new ConfigurationException("A behavior provider needs a spec");
        }

        // Get the type
        String impl = element.getAttribute(BehaviorReference.IMPLEM_ATTR_NAME);
        if (impl != null) {
            myImplem = impl;
        } else {
            throw new ConfigurationException("A behavior provider needs an implem");
        }
    }

    @Override
    public List<RequiredHandler> getRequiredHandlerList() {
        List<RequiredHandler> returnList = super.getRequiredHandlerList();
        RequiredHandler behaviorLifecycleHandler = new RequiredHandler(BehaviorReference.BEHAVIOR_LIFECYCLE_NAME,BehaviorReference.BEHAVIOR_NAMESPACE);
        returnList.add(behaviorLifecycleHandler);
        return returnList;
    }

    @Override
    public ComponentInstance createInstance(Dictionary config, IPojoContext context, HandlerManager[] handlers) throws ConfigurationException {

        InstanceManager instance = new BehaviorManager(this, context, handlers);

        try {
            instance.configure(m_componentMetadata, config);

            return instance;
        } catch (ConfigurationException e) {
            // An exception occurs while executing the configure or start
            // methods, the instance is stopped so the architecture service is still published and so we can debug
            // the issue.
            throw e;
        } catch (Throwable e) { // All others exception are handled here.
            // As for the previous case, the instance is stopped.;
            m_logger.log(Logger.INFO, "An error occurred when creating an instance of " + getFactoryName(), e);
            throw new ConfigurationException(e.getMessage(), e);
        }

    }


    /**
     * Gets the component type description of the current factory.
     *
     * @return the description of the component type attached to this factory.
     * @see IPojoFactory#getComponentTypeDescription()
     */
    public ComponentTypeDescription getComponentTypeDescription() {
        return new ContextEntityTypeDescription(this);
    }

    private class ContextEntityTypeDescription extends ComponentTypeDescription{
        public ContextEntityTypeDescription(IPojoFactory factory) {
            super(factory);
        }

        @Override
        public Dictionary getPropertiesToPublish(){
            Dictionary dict = super.getPropertiesToPublish();
            if (this.getFactory().getClassName() != null) {
                dict.put("component.class", this.getFactory().getClassName());
            }
            dict.put(BehaviorReference.BEHAVIOR_TYPE_PROPERTY,BehaviorReference.BEHAVIOR_TYPE);
            dict.put(BehaviorReference.IMPLEM_ATTR_NAME,myImplem);
            dict.put(BehaviorReference.SPEC_ATTR_NAME,mySpec);
            return dict;
        }
    }
}
