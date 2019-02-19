package fr.liglab.adele.cream.runtime.handler.functional.extension.tracker;


import java.lang.reflect.InvocationHandler;
import java.util.Arrays;

import java.util.Map;
import java.util.Dictionary;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import fr.liglab.adele.cream.annotations.internal.FunctionalExtensionReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.runtime.internal.factories.FunctionalExtensionFactory;
import fr.liglab.adele.cream.runtime.internal.factories.FunctionalExtensionInstanceManager;
import fr.liglab.adele.cream.runtime.internal.proxies.InvocationHandlerChain;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Unbind;

import org.apache.felix.ipojo.architecture.HandlerDescription;


import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;

/**
 * This handler synchronizes the life-cycle of a context entity with its extensions (tracking factories as needed, and allowing dynamic
 * reconfiguration)
 * 
 * It is also in charge of method delegation and proxy creation to handle service consumer requests.
 * 
 *
 */
@Handler(name = HandlerReference.FUNCTIONAL_EXTENSION_TRACKER_HANDLER, namespace = HandlerReference.NAMESPACE)
public class FunctionalExtensionTrackerHandler extends PrimitiveHandler implements InstanceStateListener {

    private final List<RequiredFunctionalExtension> extensions = new CopyOnWriteArrayList<>();

    private final InvocationHandlerChain delegationChain = new InvocationHandlerChain();

	/**
	 * The handler that can be used to invoke methods of the pojo associated to the active extensions of the context entity
	 */
    public InvocationHandler getDelegationHandler() {
    	return delegationChain;
    }

    /**
     * Configure part
     **/

    @Override
    public void configure(Element metadata, @SuppressWarnings("rawtypes") Dictionary configuration) throws ConfigurationException {

        Element[] behaviorElements = metadata.getElements(HandlerReference.FUNCTIONAL_EXTENSION_TRACKER_HANDLER, HandlerReference.NAMESPACE);

        if (behaviorElements == null) {
            throw new ConfigurationException("FunctionalExtension Elements are null ");
        }

        boolean hassMandatoryExtensions = false;

        for (Element element : behaviorElements) {
            Element[] behaviorIndividualElements = element.getElements(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_INDIVIDUAL_ELEMENT_NAME.toString(), "");

            if (behaviorIndividualElements == null) {
                throw new ConfigurationException("FunctionalExtension Individual Element is null ");
            }

             
            for (Element individualBehaviorElement : behaviorIndividualElements) {
                
            	String id 				= individualBehaviorElement.getAttribute(FunctionalExtensionReference.ID_ATTRIBUTE_NAME.toString());
            	boolean isMandatory 	= Boolean.parseBoolean(individualBehaviorElement.getAttribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_MANDATORY_ATTRIBUTE_NAME.toString()));
            	String specifications	= individualBehaviorElement.getAttribute(FunctionalExtensionReference.SPECIFICATION_ATTRIBUTE_NAME.toString());
            	String implementation	= individualBehaviorElement.getAttribute(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString()); 
                
            	RequiredFunctionalExtension requiredFunctionalExtension = new RequiredFunctionalExtension(id,specifications,implementation,
                        															configuration,this,isMandatory);
                
                extensions.add(requiredFunctionalExtension);
                delegationChain.addDelegate(requiredFunctionalExtension);
                
                hassMandatoryExtensions = hassMandatoryExtensions || isMandatory;
                
                String fieldAttribute = individualBehaviorElement.getAttribute(FunctionalExtensionReference.FIELD_ATTRIBUTE_NAME.toString());
                FieldMetadata fieldMetadata = null;
                if (fieldAttribute != null) {
                    fieldMetadata = getPojoMetadata().getField(fieldAttribute);
                }
                if (fieldMetadata != null) {
                    getInstanceManager().register(fieldMetadata,requiredFunctionalExtension);

                }

            }
        }


        setValidity(!hassMandatoryExtensions);
        
        getInstanceManager().addInstanceStateListener(this);

    }

    @Override
    public void reconfigure(@SuppressWarnings("rawtypes") Dictionary configuration) {
        for (RequiredFunctionalExtension extension : extensions) {
            extension.reconfigure(configuration);
        }
    }

    
    @Override
    public synchronized void start() {
    	
    	for (RequiredFunctionalExtension extension : extensions) {
                extension.start();
        }

    }

    @Override
    public void stop() {
	
    	for (RequiredFunctionalExtension extension : extensions) {
            extension.stop();
        }
    }

    public void dispose() {
    	
		for (RequiredFunctionalExtension extension : extensions) {
            extension.dispose();
        }
        
        extensions.clear();
    }

    @Override
	public void stateChanged(ComponentInstance instance, int state) {
		
		if (state == ComponentInstance.DISPOSED) {
			dispose();
		}

	}

	public void attachExtension(RequiredFunctionalExtension extension, FunctionalExtensionInstanceManager extensionInstance) {
		
		for (org.apache.felix.ipojo.Handler handler : extensionInstance.getRegisteredHandlers()) {
			if (handler instanceof FunctionalExtensionHandler) {
				((FunctionalExtensionHandler) handler).attachCore(getInstanceManager());
			}
		}

		for (org.apache.felix.ipojo.Handler handler : getInstanceManager().getRegisteredHandlers()) {
			if (handler instanceof ExtensibleEntityHandler) {
				((ExtensibleEntityHandler) handler).attachExtension(extensionInstance,extension.getSpecifications());
			}
		}

	}

	public void detachExtension(RequiredFunctionalExtension extension, FunctionalExtensionInstanceManager extensionInstance) {

		for (org.apache.felix.ipojo.Handler handler : getInstanceManager().getRegisteredHandlers()) {
			if (handler instanceof ExtensibleEntityHandler) {
				((ExtensibleEntityHandler) handler).detachExtension(extensionInstance,extension.getSpecifications());
			}
		}
		
	}
   
    public void extensionStateChanged(RequiredFunctionalExtension extension, FunctionalExtensionInstanceManager extensionInstance, int extensionState) {
    	
        if (extension.isMandatory()) {
        	
        	boolean allMandatoryValid = true;
        	
            for (RequiredFunctionalExtension mandatory : extensions) {
            	if (mandatory.isMandatory() && !mandatory.isValid()) {
            		allMandatoryValid = false;
            	}
            }

            setValidity(allMandatoryValid);
        }

    	/*
    	 * Propagate extension's state change to other interested handlers
    	 */
		for (org.apache.felix.ipojo.Handler handler : getInstanceManager().getRegisteredHandlers()) {
			if (handler instanceof ExtensibleEntityHandler) {
				((ExtensibleEntityHandler) handler).extensionStateChanged(extensionInstance,extension.getSpecifications(),extensionState);
			}
		}
        
    }


    /**
     * FunctionalExtension factory tracking part
     */

    @Bind(id = "extension", specification = Factory.class, optional = true, proxy = false, aggregate = true, filter = "(" + FunctionalExtensionReference.FUNCTIONAL_EXTENSION_FACTORY_TYPE_PROPERTY + "=" + FunctionalExtensionReference.FUNCTIONAL_EXTENSION_FACTORY_TYPE_PROPERTY_VALUE + ")")
    public synchronized void bindExtensionFactory(Factory factory, Map<String,?> properties) {
        
    	if (! (factory instanceof FunctionalExtensionFactory)) {
    		return;
    	}
    	
    	for (RequiredFunctionalExtension extension : extensions) {
            
    		String implementation 	= (String)   properties.get(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString());
            String[] specifications = (String[]) properties.get(FunctionalExtensionReference.SPECIFICATION_ATTRIBUTE_NAME.toString());
            
            if (Arrays.asList(specifications).containsAll(extension.getSpecifications())) {
            	extension.bindExtensionFactory(implementation,(FunctionalExtensionFactory) factory);
            }
        }
    }


    @Unbind(id = "extension")
    public synchronized void unbindExtensionFactory(Factory factory, Map<String,?> properties) {
    	
    	if (! (factory instanceof FunctionalExtensionFactory)) {
    		return;
    	}

        for (RequiredFunctionalExtension extension : extensions) {

        	String implementation = (String) properties.get(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString());
            
            if (extension.isExtensionFactoryBound(implementation)) {
            	extension.unbindExtensionFactory(implementation, (FunctionalExtensionFactory) factory);
            }

        }
    }

     /**
     * FunctionalExtension handler Description
     */

    @Override
    public HandlerDescription getDescription() {
        return new BehaviorHandlerDescription();
    }


    public class BehaviorHandlerDescription extends HandlerDescription {

        public BehaviorHandlerDescription() {
            super(FunctionalExtensionTrackerHandler.this);
        }

        @Override
        public Element getHandlerInfo() {
            Element element = super.getHandlerInfo();
            
            for (RequiredFunctionalExtension extension : extensions) {
                extension.getDescription(element);
            }
            
            return element;
        }
    }


}
