package fr.liglab.adele.cream.runtime.handler.entity;


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
public class EntityStateHandler extends ContextStateHandler implements ContextSource, ContextEntity, ContextListener, ExtensibleEntityHandler {

	private static final String QUALIFIED_ID = HandlerReference.NAMESPACE + ":" + HandlerReference.ENTITY_HANDLER;

    public static EntityStateHandler forInstance(InstanceManager instance) {
        return instance != null ? (EntityStateHandler) instance.getHandler(QUALIFIED_ID) : null;
    }


    private final Set<ContextStateHandler> extensions = new HashSet<>();

    private ProvidedServiceHandler 			providedServiceHandler;
    
    private ProvidedService 				providedService;
    private ProvidedServiceDescription 		providedServiceMetadata;


    @Override
    public void configure(Element element, Dictionary dictionary) throws ConfigurationException {
        super.configure(element, dictionary, HandlerReference.NAMESPACE, HandlerReference.ENTITY_HANDLER);
    }

    private synchronized void init() {
    	
    	
        providedServiceHandler = (ProvidedServiceHandler) getHandler(HandlerFactory.IPOJO_NAMESPACE + ":provides");
        
        if (providedServiceHandler == null) {
        	return;
        }

     	List<String> coreSpecifications = Collections.emptyList();
        
        for (ProvidedService candidateProvidedService : providedServiceHandler.getProvidedServices()) {
        	for (Property property : candidateProvidedService.getProperties()) {
        		
				if (property.getName().equals(fr.liglab.adele.cream.annotations.entity.ContextEntity.ENTITY_CONTEXT_SERVICES)) {
					providedService 	= candidateProvidedService;
					coreSpecifications	= property.getValue() != Property.NO_VALUE ? Arrays.asList((String[])property.getValue()) : Collections.emptyList();
				}
			}
        }

        
        if (providedService != null) {

        	for (String specification : providedService.getServiceSpecifications()) {
            	providedService.setController(HandlerReference.ENTITY_HANDLER+".controller["+specification+"]", coreSpecifications.contains(specification), specification);
    		}

            for (ProvidedServiceDescription candidateProvidedServiceMetadata : ((ProvidedServiceHandlerDescription)providedServiceHandler.getDescription()).getProvidedServices()) {
    			if (candidateProvidedServiceMetadata.getProvidedService() == providedService) {
    				providedServiceMetadata = candidateProvidedServiceMetadata;
    			}
    		}

        }
        
        /*
         * Register as my own context listener to propagate state changes to service properties
         */
        
        if (providedServiceMetadata != null) {
        	providedServiceMetadata.addProperties(super.getContext());
        }
        
        super.registerContextListener(this,null);

    }

    @Override
    public synchronized void start() {

    	if (providedServiceHandler == null) {
    		init();
    	}
    	
    	super.start();
    }
    
	@Override
	public synchronized void attachExtension(InstanceManager extension, List<String> specifications) {
		ContextStateHandler contextSourceExtension = FunctionalExtensionStateHandler.getStateHandler(extension); 
		extensions.add(contextSourceExtension);
		
		for (Map.Entry<ContextListener,List<String>> listenerRegistration : contextSourceListeners.entrySet()) {
			
			ContextListener listener = listenerRegistration.getKey();
			String[] properties		 = listenerRegistration.getValue() != null ? listenerRegistration.getValue().toArray(new String[0]) : null;
			
			contextSourceExtension.registerContextListener(listener, properties);
		}
		
	}

	@Override
	public synchronized void detachExtension(InstanceManager extension, List<String> specificatons) {
		ContextSource contextSourceExtension = FunctionalExtensionStateHandler.getStateHandler(extension); 
		extensions.remove(contextSourceExtension);
		
		for (ContextListener listener : contextSourceListeners.keySet()) {
			contextSourceExtension.unregisterContextListener(listener);
		}

	}

    /**
     * Update provided services to reflect extension's validity changes
     * 
     */
	@Override
	public void extensionStateChanged(InstanceManager extension, List<String> specifications, int extensionState) {
    	
		if (providedServiceHandler == null || providedServiceMetadata == null) {
    		return;
    	}
    	
		ContextStateHandler contextSourceExtension = FunctionalExtensionStateHandler.forInstance(extension);
		
		if (extensionState == ComponentInstance.VALID) {

			for (String specification : specifications) {
	        	providedServiceHandler.onSet(null,HandlerReference.ENTITY_HANDLER+".controller["+specification+"]", true);
			}
			
			providedServiceMetadata.addProperties(contextSourceExtension.getContext());
		}
		
		if (extensionState == ComponentInstance.INVALID) {

			for (String specification : specifications) {
	        	providedServiceHandler.onSet(null,HandlerReference.ENTITY_HANDLER+".controller["+specification+"]", false);
			}
			
			/* TODO Because of a bug in iPOJO we cannot delete properties in batch mode. As a workaround, we do it one by
			 * one, this has the effect of producing a cascade of intermediate notifications
			 *
			 * Dictionary context = contextSourceExtension.getContext();
			 * context.remove(ContextEntity.CONTEXT_ENTITY_ID);
			 * providedServiceMetadata.removeProperties(context);
			 */   
			  
			
			for (Enumeration<?> states = contextSourceExtension.getContext().keys(); states.hasMoreElements();) {
				String property = (String) states.nextElement();
				if (!property.equals(ContextEntity.CONTEXT_ENTITY_ID)) {
					update(contextSourceExtension,property,null);
				}
			} 
		}

	}

	/**
	 * Update provided services properties to reflect state changes (handles both myself and extensions)
	 */
	@Override
	public void update(ContextSource source, String property, Object value) {

		if (providedServiceMetadata == null) {
    		return;
    	}

		Hashtable<String,Object> properties = new Hashtable<>();
		properties.put(property, value != null ? value : "TO_BE_REMOVED");
		
		if (value != null) {
			providedServiceMetadata.addProperties(properties);
		}
		else {
			providedServiceMetadata.removeProperties(properties);
		}
	}

	@Override
	public Dictionary getContext() {
		
		Hashtable<String,Object> context = new Hashtable<>();
		
		putAll(context,super.getContext());
		
		for (ContextSource extension : extensions) {
			putAll(context, extension.getContext());
		}
		return context;
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

   /**
     * Given an iPOJO object with the entity handler attached, return the associated context entity
     */
    public static ContextEntity getContextEntity(Pojo pojo) {
        return getStateHandler((InstanceManager)pojo.getComponentInstance());
    }

    /**
     * Given an iPOJO instance with the entity handler attached, return the associated context entity
     */
    public static ContextStateHandler getStateHandler(InstanceManager instance) {
        return instance != null ? (ContextStateHandler) instance.getHandler(QUALIFIED_ID) : null;
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
    private static final <K,V> void putAll(Dictionary<K,V> self, Dictionary<? extends K, ? extends V> m) {
    	
    	Enumeration<? extends K> keys = m.keys();
    	while(keys.hasMoreElements()) {
    		K key = keys.nextElement();
    		if (self.get(key) == null) {
    			self.put(key, m.get(key));
    		}
    	}
    	
    }

    
}
