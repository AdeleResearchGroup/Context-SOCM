package fr.liglab.adele.cream.runtime.handler.creator;

import org.apache.felix.ipojo.Factory;
import org.osgi.framework.ServiceReference;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.architecture.ComponentTypeDescription;

/**
 * This class provides the basic implementation of a creator factory object for iPOJO instances.
 * <p>
 * The creator is in charge of keeping a list of created instances, and dynamically handle availability
 * of the corresponding iPOJO factory.
 * <p>
 * If a factory is no longer available, all instances are automatically disposed, but the creator keeps
 * enough information to try to recreate them when the factory will be available again.
 * <p>
 * All requests for creation of components that are received while the factory is unavailable will be
 * remembered, and be automatically processed when the factory will be available again.
 * <p>
 * It is also possible to programmatically enable/disable the creator. This has the same effect as changing
 * the availability of the factory.
 */
public abstract class ComponentCreator {

    /**
     * The current state of the creator
     */
    private boolean enabled = true;

    /**
     * The list of instances created by this creator (both instantiated and pending)
     */
    private Map<String, InstanceDeclaration> instances = new ConcurrentHashMap<>();


    /**
     * The iPOJO component factory used to create the instance
     */
    private Factory factory;

    /**
     * Get the dscription of this creator
     */
    public abstract String getDescription();
    
    /**
     * Creates a new instance declaration and add it to the list of handled declarations
     */
    protected final void instantiate(InstanceDeclaration instance) {

    	instances.put(instance.getName(), instance);
    	
    	if (isEnabled() && factory != null) {
            instance.instantiate(factory);
        }
        
    }

    /**
     * Destroys the specified instance and remove it from the list of created items
     */
    protected final void dispose(String id) {
        
    	InstanceDeclaration instance = instances.remove(id);
        
        if (instance != null) {
            instance.dispose();
        }
    }

	protected final InstanceDeclaration get(String id) {
        return id != null ? instances.get(id) : null;
    }

	protected final Set<String> ids() {
		return instances.keySet();
	}
	
    /**
     * The current state of the creator
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Change the status of this creator
     */
    public boolean setEnabled(boolean enabled) {

        if (enabled == this.enabled) {
            return enabled;
        }

        boolean wasEnabled = this.enabled;
        this.enabled = enabled;

        if (this.isEnabled()) {
            instantiatePending();
        } else {
            disposeInstantiated();
        }

        return wasEnabled;
    }

    /**
     * Whether this is the factory used by this creator
     */
    protected abstract boolean shouldBind(ServiceReference<Factory> referenceFactory);

    /**
     * Bind the iPOJO factory to this creator.
     * <p>
     * This will trigger instantiation of pending items if the creator is enabled
     */
    protected final void bindFactory(Factory factory) {
        this.factory = factory;
        instantiatePending();
    }

    /**
     * Unbinds the iPOJO factory.
     * <p>
     * All requested creations will be delayed (in the pending list) until a factory is bound again
     */
    protected final void unbindFactory() {
        this.factory = null;
        disposeInstantiated();
    }

	protected ComponentTypeDescription getComponentDescrition() {
		return factory != null ? factory.getComponentDescription() : null;
	}

    /**
     * Tries to instantiate all pending instances, if the creator is enabled.
     * <p>
     * NOTE notice that this method is invoked by changes of the status of the creator, or by changes in the
     * availability of the iPOJO factory used to instantiate items
     */
    private void instantiatePending() {

        if (!isEnabled())
            return;

        for (InstanceDeclaration instance : instances.values()) {
            if (!instance.isInstantiated()) {
                instance.instantiate(factory);
            }
        }
    }


    /**
     * Tries to dispose all instantiated instances, if the creator is disabled.
     * <p>
     * NOTE notice that this method is invoked by changes of the status of the creator, or by changes in the
     * availability of the iPOJO factory used to instantiate items
     */
    private void disposeInstantiated() {
        for (InstanceDeclaration instance : instances.values()) {
            if (instance.isInstantiated()) {
                instance.dispose();
            }
        }
    }

}