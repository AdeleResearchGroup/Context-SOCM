package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.util.Logger;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

/**
 * Created by aygalinc on 31/05/16.
 */
public class ContextEntityFactory extends ComponentFactory {

    public ContextEntityFactory(BundleContext context, Element element) throws ConfigurationException {
        super(context, element);
    }

    @Override
    public ComponentInstance createInstance(Dictionary config, IPojoContext context, HandlerManager[] handlers) throws org.apache.felix.ipojo.ConfigurationException {

        InstanceManager instance = new ContextEntityManager(this, context, handlers);

        try {
            instance.configure(m_componentMetadata, config);
            instance.start();
            return instance;
        } catch (ConfigurationException e) {
            // An exception occurs while executing the configure or start
            // methods, the instance is stopped so the architecture service is still published and so we can debug
            // the issue.
            instance.stop();
            throw e;
        } catch (Throwable e) { // All others exception are handled here.
            // As for the previous case, the instance is stopped.
            instance.stop();
            m_logger.log(Logger.INFO, "An error occurred when creating an instance of " + getFactoryName(), e);
            throw new ConfigurationException(e.getMessage(), e);
        }

    }

    @Override
    public List<RequiredHandler> getRequiredHandlerList(){
        /**    List<RequiredHandler> requiredHandlers = super.getRequiredHandlerList();
      List<RequiredHandler> list = new ArrayList<>();


         * Remove behavior from the required handler list

        for (RequiredHandler handler: requiredHandlers){
            if  (!(BehaviorReference.BEHAVIOR_NAMESPACE.equals(handler.getNamespace()) && BehaviorReference.DEFAULT_BEHAVIOR_TYPE.equals(handler.getName()))){
                list.add(handler);
            }
        } */
        return super.getRequiredHandlerList();
    }

    /**
     * Gets the component type description of the current factory.
     *
     * @return the description of the component type attached to this factory.
     * @see org.apache.felix.ipojo.IPojoFactory#getComponentTypeDescription()
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
            return dict;
        }
    }
}
