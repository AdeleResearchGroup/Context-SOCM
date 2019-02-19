package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.annotations.internal.FunctionalExtensionReference;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.ParseUtils;
import org.apache.felix.ipojo.util.Logger;
import org.osgi.framework.BundleContext;

import java.util.Dictionary;

/**
 * Created by aygalinc on 31/05/16.
 */
public class FunctionalExtensionFactory extends ComponentFactory {

    private final String[] mySpec;

    private final String myImplem;

    public FunctionalExtensionFactory(BundleContext context, Element element) throws ConfigurationException {
        super(context, element);
        // Get the type
        String[] spec = ParseUtils.parseArrays(element.getAttribute(FunctionalExtensionReference.SPECIFICATION_ATTRIBUTE_NAME.toString()));
        if (spec != null) {
            mySpec = spec;
        } else {
            throw new ConfigurationException("A behavior provider needs a spec");
        }

        // Get the type
        String impl = element.getAttribute(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString());
        if (impl != null) {
            myImplem = impl;
        } else {
            throw new ConfigurationException("A behavior provider needs an implem");
        }
    }

    @Override
    public ComponentInstance createInstance(@SuppressWarnings("rawtypes") Dictionary configuration, IPojoContext context, HandlerManager[] handlers) throws ConfigurationException {

        InstanceManager instance = new FunctionalExtensionInstanceManager(this, context, handlers);

        try {
            instance.configure(m_componentMetadata, configuration);

            return instance;
        } catch (ConfigurationException e) {
            // An exception occurs while executing the configure or start
            // methods, the instance is stopped so the architecture service is still published and so we can debug
            // the issue.
            throw e;
        } catch (Exception e) { // All others exception are handled here.
            // As for the previous case, the instance is stopped
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
    @Override
    public ComponentTypeDescription getComponentTypeDescription() {
        return new BehaviorTypeDescription(this);
    }

    private class BehaviorTypeDescription extends ComponentTypeDescription {
        public BehaviorTypeDescription(IPojoFactory factory) {
            super(factory);
        }

        @Override
        public Dictionary<String,Object> getPropertiesToPublish() {
            Dictionary<String,Object> published = super.getPropertiesToPublish();
            
            if (this.getFactory().getClassName() != null) {
            	published.put("component.class", this.getFactory().getClassName());
            }
            
            published.put(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_FACTORY_TYPE_PROPERTY, FunctionalExtensionReference.FUNCTIONAL_EXTENSION_FACTORY_TYPE_PROPERTY_VALUE);
            published.put(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString(), myImplem);
            published.put(FunctionalExtensionReference.SPECIFICATION_ATTRIBUTE_NAME.toString(), mySpec);
            
            return published;
        }
    }
}
