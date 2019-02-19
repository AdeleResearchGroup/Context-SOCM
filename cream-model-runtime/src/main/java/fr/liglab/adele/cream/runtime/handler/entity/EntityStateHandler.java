package fr.liglab.adele.cream.runtime.handler.entity;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedService;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceDescription;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandlerDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.util.Property;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.runtime.handler.entity.utils.ContextStateHandler;
import fr.liglab.adele.cream.runtime.handler.functional.extension.tracker.ExtensibleEntityHandler;

import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

/**
 * This handler acts as a composite state handler. It inherits management of the locally defined states and also aggregates the states of
 * the attached extensions.
 * 
 * This handler is also in charge of propagating the entity state as properties of the provided services.
 * 
 *
 */
@Handler(name = HandlerReference.ENTITY_HANDLER, namespace = HandlerReference.NAMESPACE)
public class EntityStateHandler extends ContextStateHandler implements ContextSource, ContextEntity, ExtensibleEntityHandler {

	private static final String QUALIFIED_ID = HandlerReference.NAMESPACE + ":" + HandlerReference.ENTITY_HANDLER;

    /**
     * Given an iPOJO instance with the entity handler attached, return the associated context entity
     */
    public static ContextStateHandler forInstance(InstanceManager instance) {
        return instance != null ? (EntityStateHandler) instance.getHandler(QUALIFIED_ID) : null;
    }

    /**
     * Given an iPOJO object with the entity handler attached, return the associated context entity
     */
    public static ContextEntity getContextEntity(Pojo pojo) {
        return EntityStateHandler.forInstance((InstanceManager)pojo.getComponentInstance());
    }

    /**
     * The functional extensions attached to this entity
     */
    private final Set<ContextStateHandler> extensions = new HashSet<>();

    /**
     * The object responsible of propagating state changes (in core and extensions) to the published
     * services' properties
     */
    private ServicePublication	servicePublication;

    
    @Override
    public void configure(Element element,  @SuppressWarnings("rawtypes") Dictionary dictionary) throws ConfigurationException {
        super.configure(element, dictionary, HandlerReference.NAMESPACE, HandlerReference.ENTITY_HANDLER);
    }


    @Override
    public synchronized void start() {

    	/*
    	 * This handler has low priority, so we are sure that this method is called after the provider handler has been
    	 * started and the provided service metadata initialized
    	 */
    	if (servicePublication == null) {
    		servicePublication = new ServicePublication((ProvidedServiceHandler) getHandler(HandlerFactory.IPOJO_NAMESPACE + ":provides"));
            super.registerContextListener(servicePublication,null);
    	}

    	super.start();
    }

    @Override
    public void stateChanged(int state) {

    	super.stateChanged(state);
    	
    	/*
    	 * When the context entity core is invalidated, the entity services are no longer provided and are
    	 * unregistered from the registry. However, this usually produces a cascade of state events that try
    	 * to update re-entrantly the service description that is being unregistered.
    	 * 
    	 * We try to avoid this situation by suspending propagation during the invalid period, while keeping
    	 * track of the updates, and delaying them until revalidation.
    	 */
        if (state == InstanceManager.INVALID) {
        	servicePublication.suspend();
        }

    	if (state == InstanceManager.VALID) {
    		servicePublication.resume();
        }


    }

    /**
     * Update provided services to reflect extension's validity changes
     * 
     */
	@Override
	public void extensionStateChanged(InstanceManager extension, List<String> specifications, int extensionState) {
    	
		ContextSource extensionContext = FunctionalExtensionStateHandler.forInstance(extension);
		
		if (extensionState == ComponentInstance.VALID) {
			servicePublication.publish(specifications,extensionContext);
		}
		
		if (extensionState == ComponentInstance.INVALID) {
			servicePublication.unpublish(specifications,extensionContext);
		}

	}


	@Override
	public Object getProperty(String property) {
		
		Object value =  super.getProperty(property);
		if (value != null) {
			return value;
		}
		
		for (ContextSource extension : extensions) {
			value = extension.getProperty(property);
			if (value != null) {
				return value;
			}
		}
		
		return value;
	}

	
	
	@Override
	public Dictionary<String,?> getContext() {
		
		Hashtable<String,Object> context = new Hashtable<>();
		
		putAll(context,super.getContext(),true);
		
		for (ContextSource extension : extensions) {
			
			@SuppressWarnings("unchecked")
			Dictionary<String,?> extendedContext = extension.getContext();
			
			putAll(context, extendedContext, false);
		}
		
		return context;
	}


	@Override
	public void registerContextListener(ContextListener listener, String[] properties) {
		
		super.registerContextListener(listener,properties);
		
		for (ContextSource extension : extensions) {
			extension.registerContextListener(listener, properties);
		}
	}
	
	@Override
	public synchronized void unregisterContextListener(ContextListener listener) {
		
		super.unregisterContextListener(listener);
		
		for (ContextSource extension : extensions) {
			extension.unregisterContextListener(listener);
		}
	}


	@Override
	public synchronized void attachExtension(InstanceManager extension, List<String> specifications) {
		ContextStateHandler contextSourceExtension = FunctionalExtensionStateHandler.forInstance(extension); 
		extensions.add(contextSourceExtension);
		
		for (Map.Entry<ContextListener,List<String>> listenerRegistration : contextSourceListeners.entrySet()) {
			
			ContextListener listener = listenerRegistration.getKey();
			String[] properties		 = listenerRegistration.getValue() != null ? listenerRegistration.getValue().toArray(new String[0]) : null;
			
			contextSourceExtension.registerContextListener(listener, properties);
		}
		
	}

	@Override
	public synchronized void detachExtension(InstanceManager extension, List<String> specificatons) {
		ContextSource contextSourceExtension = FunctionalExtensionStateHandler.forInstance(extension); 
		extensions.remove(contextSourceExtension);
		
		for (ContextListener listener : contextSourceListeners.keySet()) {
			contextSourceExtension.unregisterContextListener(listener);
		}

	}


    @Override
    public Set<String> getServices() {
    	Set<String> services = new HashSet<>(super.getServices());
    	
    	for (ContextEntity extension : extensions) {
    		services.addAll(extension.getServices());
		}
    	
        return services;
    }

    @Override
    public Set<String> getStates() {
    	Set<String> states = new HashSet<>(super.getStates());
    	
    	for (ContextEntity extension : extensions) {
    		states.addAll(extension.getStates());
		}
    	
        return states;
    }

    @Override
    public Object getValue(String state) {
    	
		Object value =  super.getValue(state);
		if (value != null) {
			return value;
		}
		
		for (ContextEntity extension : extensions) {
			value = extension.getValue(state);
			if (value != null) {
				return value;
			}
		}
		
		return value;
    }

    @Override
    public Map<String, Object> getValues() {
		
    	Map<String,Object> values = new HashMap<>(super.getValues());
		for (ContextEntity extension : extensions) {
			values.putAll(extension.getValues());
		}
		
		return values;
    }

    private static class ServicePublication implements  ContextListener {

    	private final ProvidedServiceHandler 		providedServiceHandler;
        
        private final ProvidedServiceDescription 	registration;

        private boolean	suspended;
        
        private Dictionary<String,Object> 	propertiesToAdd;
        private Dictionary<String,Object> 	propertiesToRemove;
        
        private List<String>		specificationsToPublish;
        private List<String>		specificationsToUnpublish;
        
        public ServicePublication(ProvidedServiceHandler providedServiceHandler) {

           	/*
           	 * By default, suspended propagation
           	 */
        	suspend();

            /*
             * Get a reference to the service handler and the entity provided services 
             * reguistration that needs to be udopated
             */
        	this.providedServiceHandler = providedServiceHandler;

            if (providedServiceHandler == null) {
            	registration = null;
            	return;
            }

            /*
             * Look for the entry in the provided service handler that corresponds to the entity services
             */
         	List<String> coreSpecifications	= Collections.emptyList();
         	ProvidedService providedService	= null;

         	for (ProvidedService candidate : providedServiceHandler.getProvidedServices()) {
            	for (Property property : candidate.getProperties()) {
            		
    				if (property.getName().equals(fr.liglab.adele.cream.annotations.entity.ContextEntity.ENTITY_CONTEXT_SERVICES)) {
    					providedService 	= candidate;
    					coreSpecifications	= property.getValue() != Property.NO_VALUE ? Arrays.asList((String[])property.getValue()) : Collections.emptyList();
    				}
    			}
            }

            
            if (providedService == null) {
            	registration = null;
            	return;
            }

           	ProvidedServiceDescription serviceDescription = null;
           	for (ProvidedServiceDescription candidate : ((ProvidedServiceHandlerDescription)providedServiceHandler.getDescription()).getProvidedServices()) {
           		if (candidate.getProvidedService() == providedService) {
           			serviceDescription = candidate;
        		}
        	}

           	if (serviceDescription == null) {
           		registration = null;
           		return;
           	}
           	
           	this.registration = serviceDescription;
           	
            /*
             * Add controller to be able to publish/unpublish extensions selectively
             */

           	for (String specification : providedService.getServiceSpecifications()) {
           		providedService.setController(HandlerReference.ENTITY_HANDLER+".controller["+specification+"]", coreSpecifications.contains(specification), specification);
       		}

        }
        
        /**
    	 * Update provided services properties to reflect state changes (handles both core and extensions)
    	 */
    	@Override
    	public void update(ContextSource source, String property, Object value) {

    		Dictionary<String,Object> properties = new Hashtable<>();
    		properties.put(property, value != null ? value : "TO_BE_REMOVED");
    		
    		if (value != null) {
    			addProperties(properties);
    		}
    		else {
    			removeProperties(properties);
    		}
    	}

		public void publish(List<String> specifications, ContextSource source) {

        	publish(specifications);

        	@SuppressWarnings("unchecked")
			Dictionary<String,Object> properties = source.getContext();
			addProperties(properties);
        }
        
        public void unpublish(List<String> specifications, ContextSource source) {

        	unpublish(specifications);
        	
        	@SuppressWarnings("unchecked")
			Dictionary<String,Object> properties = source.getContext();
        	properties.remove(ContextEntity.CONTEXT_ENTITY_ID);
        	
        	removeProperties(properties);
        }

        private void addProperties(Dictionary<String,?> properties) {
        	addProperties(properties,false);
        }
        
        private void addProperties(Dictionary<String,?> properties, boolean forced) {

        	if ( (!forced) && suspended) {

        		synchronized (this) {
            		putAll(propertiesToAdd,properties,true);
        			removeAll(propertiesToRemove,properties);
            		return;
				}
        	}

        	if (registration != null) {
    			registration.addProperties(properties);
        	}
        }

        private void removeProperties(Dictionary<String,?> properties) {
        	removeProperties(properties,false);
        }
        
        private void removeProperties(Dictionary<String,?> properties, boolean forced) {

        	if ( (!forced) && suspended) {

        		synchronized (this) {
            		putAll(propertiesToRemove,properties,true);
            		removeAll(propertiesToAdd,properties);
            		return;
				}
        	}

			/* TODO Because of a bug in iPOJO we cannot delete properties in batch mode. As a 
			 * workaround, we do it one by one, this has the effect of producing a cascade of
			 * intermediate notifications
			 *
			 */   

        	if (registration != null) {
    			for (Enumeration<String> toRemove = properties.keys() ; toRemove.hasMoreElements() ; ) {
    	    		Hashtable<String,Object> property = new Hashtable<>();
    	    		property.put(toRemove.nextElement(),"TO_BE_REMOVED");
    	        	registration.removeProperties(property);
    			} 
        	}

        }

        private void publish(List<String> specifications) {
        	publish(specifications,false);
        }
        
        private void publish(List<String> specifications, boolean forced) {
        	
        	if ( (!forced) && suspended) {

        		synchronized (this) {
            		specificationsToPublish.addAll(specifications);
            		specificationsToUnpublish.removeAll(specifications);
            		return;
				}
        	}

        	if (providedServiceHandler != null) {
    			for (String specification : specifications) {
    	        	providedServiceHandler.onSet(null,HandlerReference.ENTITY_HANDLER+".controller["+specification+"]", true);
    			}
        	}

        }

        private void unpublish(List<String> specifications) {
        	unpublish(specifications,false);
        }
        
        private void unpublish(List<String> specifications, boolean forced) {
        	
        	if ( (!forced) && suspended) {

        		synchronized (this) {
            		specificationsToUnpublish.addAll(specifications);
            		specificationsToPublish.removeAll(specifications);
            		return;
				}
        	}

        	if (providedServiceHandler != null) {
				for (String specification : specifications) {
		        	providedServiceHandler.onSet(null,HandlerReference.ENTITY_HANDLER+".controller["+specification+"]", false);
				}
        	}
        }

        public void suspend() {
            
        	if (suspended) {
        		return;
        	}
        	
        	synchronized (this) {

        		suspended = true;
                
                propertiesToAdd 			= new Hashtable<>();
                propertiesToRemove			= new Hashtable<>();
                specificationsToPublish 	= new ArrayList<>();
                specificationsToUnpublish 	= new ArrayList<>();
			}
        }
        
        public void resume() {

        	if (!suspended) {
        		return;
        	}

        	
        	synchronized (this) {

            	suspended = false;

        		unpublish(specificationsToUnpublish,true);
        		publish(specificationsToPublish,true);
        		removeProperties(propertiesToRemove,true);
        		addProperties(propertiesToAdd,true);
        		
        		propertiesToAdd 			= null;
        		propertiesToRemove			= null;
        		specificationsToPublish		= null;
        		specificationsToUnpublish	= null;
			}
        	
        }
   	
    }

    /**
     * The Wisdom Scheduler used to handle periodic tasks
     */
    @Requires(id = "scheduler", proxy = false, optional = false)
    public ManagedScheduledExecutorService scheduler;

    @Override
    protected ManagedScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     * Utility methods to manipulate dictionaries
     */
    private static final <K,V> void putAll(Dictionary<K,V> self, Dictionary<? extends K, ? extends V> m, boolean override) {
    	
    	Enumeration<? extends K> keys = m.keys();
    	while(keys.hasMoreElements()) {
    		K key = keys.nextElement();
    		if (self.get(key) == null || override) {
    			self.put(key, m.get(key));
    		}
    	}
    	
    }

    private static final <K,V> void removeAll(Dictionary<K,V> self, Dictionary<? extends K, ?> m) {
    	
    	Enumeration<? extends K> keys = m.keys();
    	while(keys.hasMoreElements()) {
    		K key = keys.nextElement();
    		self.remove(key);
    	}
    	
    }

    
}
