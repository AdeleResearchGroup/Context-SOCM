package fr.liglab.adele.cream.runtime.handler.creator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.felix.ipojo.Factory;
import org.osgi.framework.ServiceReference;

/**
 * This class provides the basic implementation of a creator factory object for iPOJO instances.
 * 
 * The creator is in charge of keeping a list of created instances, and dynamically handle availability
 * of the corresponding iPOJO factory.
 * 
 * If a factory is no longer available, all instances are automatically disposed, but the creator keeps
 * enough information to try to recreate them when the factory will be available again.
 * 
 * All requests for creation of components that are received while the factory is unavailable will be 
 * remembered, and be automatically processed when the factory will be available again.
 * 
 * It is also possible to programmatically enable/disable the creator. This has the same effect as changing
 * the availability of the factory.
 *
 */
public abstract class ComponentCreator {
	
	/**
	 * The description of this creator
	 */
	protected final String description;

   	/**
   	 * The current state of the creator
   	 * 
   	 */
   	protected boolean enabled;
	
   	/**
	 * The list of instances created by this creator (both instantiated and pending)
	 */
   	protected Map<String,InstanceDeclaration> instances = new ConcurrentHashMap<>();
   	
	
  	/**
	 * The iPOJO component factory used to create the instance
	 */
	private Factory factory;
	
	protected ComponentCreator(String description) {
		this.description 	= description;
		this.enabled		= true;
	}

	/**
	 * A description of the creator
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Creates a new instance declaration and add it to the list of handled declarations
	 */
	protected void create(InstanceDeclaration instance) {
		instances.put(instance.getName(),instance);
		if (isEnabled() && factory != null) {
			instance.instantiate(factory);
		}
			
	}
	
	/**
	 * Destroys the specified instance and remove it from the list of created items
	 */
	protected void delete(String id) {
		InstanceDeclaration instance = instances.get(id);
		if (instance != null) {
			instance.dispose();
			instances.remove(id);
		}
	}
	
	/**
	 * Get the list of all instances
	 */
	public Stream<InstanceDeclaration> getInstanceDeclarations() {
		return instances.values().stream();
	}
	
	/**
	 * Get the list of instances matching a given predicate
	 */
	public Stream<InstanceDeclaration> getInstanceDeclarations(Predicate<InstanceDeclaration> filter) {
		return instances.values().stream().filter(filter);
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
		boolean wasEnabled = this.enabled;
		this.enabled = enabled;
		
		if (this.isEnabled()) {
			instantiatePending();
		}
		else {
			disposeInstantiated();
		}
		
		return wasEnabled;
	}
	
	/**
	 * Whether this is the factory used by this creator
	 */
	public abstract boolean shouldBind(ServiceReference<Factory> referenceFactory);

	/**
	 * Bind the iPOJO factory to this creator. 
	 * 
	 * This will trigger instantiation of pending items if the creator is enabled
	 * 
	 */
    public void bindFactory(Factory factory) {
    	this.factory = factory;
    	instantiatePending();
    }

    /**
     * Unbinds the iPOJO factory.
     * 
     * All requested creations will be delayed (in the pending list) until a factory is bound again
     */
    public void unbindFactory() {
    	this.factory = null;
    	disposeInstantiated();
    }
	
	/**
	 * Tries to instantiate all pending instances, if the creator is enabled.
	 * 
	 * NOTE notice that this method is invoked by changes of the status of the creator, or by changes in the
	 * availability of the iPOJO factory used to instantiate items
	 */
	protected void instantiatePending() {
		
		if (!isEnabled())
			return;
		
		for (InstanceDeclaration instance : instances.values()) {
			if (! instance.isInstantiated()) {
				instance.instantiate(factory);
			}
		}
	}
	
	
	/**
	 * Tries to dispose all instantiated instances, if the creator is disabled.
	 * 
	 * NOTE notice that this method is invoked by changes of the status of the creator, or by changes in the
	 * availability of the iPOJO factory used to instantiate items
	 */
	protected void disposeInstantiated() {
		for (InstanceDeclaration instance : instances.values()) {
			if (instance.isInstantiated()) {
				instance.dispose();
			}
		}
	}
	

}