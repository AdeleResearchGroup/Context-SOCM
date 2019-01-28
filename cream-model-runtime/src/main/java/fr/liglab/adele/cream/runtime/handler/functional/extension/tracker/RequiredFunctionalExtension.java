package fr.liglab.adele.cream.runtime.handler.functional.extension.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import fr.liglab.adele.cream.annotations.internal.FunctionalExtensionReference;
import fr.liglab.adele.cream.runtime.handler.entity.FunctionalExtensionStateHandler;
import fr.liglab.adele.cream.runtime.internal.factories.FunctionalExtensionFactory;
import fr.liglab.adele.cream.runtime.internal.factories.FunctionalExtensionInstanceManager;

import fr.liglab.adele.cream.utils.SuccessorStrategy;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.ParseUtils;


/**
 * Created by aygalinc on 02/06/16.
 */
/** TODO : Synchronisation seems messy ...
**/
public class RequiredFunctionalExtension implements InvocationHandler, FieldInterceptor, InstanceStateListener {

    private static final Logger LOG = LoggerFactory.getLogger(RequiredFunctionalExtension.class);


 
    private final FunctionalExtensionTrackerHandler parent;

    private final String id;

    private final List<String> specifications;

    private final String specificationsAttribute;

    private final boolean mandatory;

    private final Dictionary<String,Object> instanceConfiguration = new Hashtable<>();

    private final Map<String,FunctionalExtensionFactory> extensionFactories = new ConcurrentHashMap<>();

    private String selectedImplementation;

    private FunctionalExtensionFactory selectedFactory;

    private FunctionalExtensionInstanceManager extension;



    public RequiredFunctionalExtension(String id, String specifications, String immplementation, Dictionary<?,?> configuration, FunctionalExtensionTrackerHandler parent, boolean mandatory) {
    	
    	this.id							= id;
    	this.specificationsAttribute	= specifications;
    	this.specifications				= Arrays.asList(ParseUtils.parseArrays(specificationsAttribute));
        this.mandatory					= mandatory;

        this.parent 					= parent;

        /**
         * Extract Dictionary properties
         */
        Enumeration<?> properties = configuration.keys();
        while (properties.hasMoreElements()) {
            Object property	= properties.nextElement();
            Object value 	= configuration.get(property);
            
            if (!("instance.name".equals(property))) {
                instanceConfiguration.put((String)property,value);
            }
        }
        
        instanceConfiguration.put(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_ID_CONFIG.toString(),id);

        this.selectedImplementation	= immplementation;
        this.selectedFactory		= null;
    }

    public void reconfigure(Dictionary<?,?> configuration) {
    	
        @SuppressWarnings("unchecked")Map<String,String> functionalExtensionConfiguration = 
        			(Map<String, String>) configuration.get(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_RECONFIGURATION.toString());
            
        if (functionalExtensionConfiguration != null) {
        	
        	String id 				= functionalExtensionConfiguration.get(FunctionalExtensionReference.ID_ATTRIBUTE_NAME.toString());
            String implementattion	= functionalExtensionConfiguration.get(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString());

            if (id != null && id.equals(this.id)) {
            	setSelectedImplementation(implementattion);
            }        	
        }

        if (extension != null) {
            extension.reconfigure(configuration);
        }
    }

    public synchronized void setSelectedImplementation(String selectedImplementation) {
        
    	if ( selectedImplementation == null) {
            LOG.warn("The reconfiguration cannot be done because functional implementation class is null" );
            return;
        }
    	
    	FunctionalExtensionFactory factory = extensionFactories.get(selectedImplementation);

        if (factory == null){
            LOG.warn("The reconfiguration cannot be done because there is no factory for implementation class" + selectedImplementation);
            return;
        }

        uninstantiate();
        
        this.selectedImplementation = selectedImplementation;
        this.selectedFactory 		= factory;
        
        instantiate();
    }

	public List<String> getSpecifications() {
		return specifications;
	}

    public synchronized boolean isValid() {
        return extension != null && extension.getState() == ComponentInstance.VALID;
    }

    public Dictionary<?,?> getContext() {
    	return extension != null ? FunctionalExtensionStateHandler.forInstance(extension).getContext() : new Properties();
    }

	public boolean isMandatory() {
		return mandatory;
	}

	public boolean isExtensionFactoryBound(String implementation) {
		return extensionFactories.containsKey(implementation);
	}

	public synchronized void bindExtensionFactory(String implementation, FunctionalExtensionFactory factory) {
		extensionFactories.put(implementation, factory);
		
		if (selectedFactory == null && selectedImplementation.equalsIgnoreCase(implementation)) {
			
			selectedFactory = factory;
			instantiate();
		}
	}

	public synchronized void unbindExtensionFactory(String implementation, FunctionalExtensionFactory factory) {
		extensionFactories.remove(implementation);
		
		if (selectedFactory == factory) {
			uninstantiate();
			selectedFactory = null;
		}
		
	}

    public synchronized void start() {
    	if (extension != null) {
        	extension.start();
        }
    }

    public synchronized void stop() {
        
    	if (extension != null) {
        	extension.stop();
        }
    }


    public synchronized void dispose() {
        
    	uninstantiate();
    	
    	selectedImplementation 	= null;
    	selectedFactory = null;
    	
    	extensionFactories.clear();
    }


    private void instantiate() {
    	
        if (selectedFactory == null) {
            LOG.warn("Try to add new functional extension manager but the factory of the functional extension is null");
            return;
        }
        
        if (extension != null ) {
            LOG.warn("Try to add new functional extension manager but the extension currently have one");
            return;
        }

        try {
        	
        	extension = (FunctionalExtensionInstanceManager) selectedFactory.createComponentInstance(instanceConfiguration);
        	extension.addInstanceStateListener(this);
 
           	parent.attachExtension(this,extension);

        	if (parent.getInstanceManager().isStarted()) {
        		start();
        	}
        	
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            LOG.error(UnacceptableConfiguration.class.getName(), unacceptableConfiguration);
        } catch (MissingHandlerException e) {
            LOG.error(MissingHandlerException.class.getName(), e);
        } catch (ConfigurationException e) {
            LOG.error(ConfigurationException.class.getName(), e);
        }
    }

    private void uninstantiate() {
    	if (extension != null) {
            parent.detachExtension(this,extension);
            extension.dispose();
        }
    }


	@Override
	public void stateChanged(ComponentInstance extension, int extensionState) {

		parent.extensionStateChanged(this,this.extension,extensionState);

		if (extensionState == ComponentInstance.DISPOSED) {
			this.extension = null;
		}
		
	}

	@Override
	public void onSet(Object pojo, String fieldName, Object value) {
	}

	@Override
	public Object onGet(Object pojo, String fieldName, Object value) {
        return extension != null ? extension.getPojoObject() : null;
	}

	
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    	
    	InvocationHandler handler = null;
    	
        synchronized (this) {
            handler = extension != null && extension.isStarted() ? extension.getInvocationHandler() : null;
        }

        return handler != null ? handler.invoke(proxy, method, args) : SuccessorStrategy.NO_FOUND_CODE;
    }


    public synchronized void getDescription(Element elementToAttach) {
    	
        Element extensionElement = new Element(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_INDIVIDUAL_ELEMENT_NAME.toString(),"");
        extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.ID_ATTRIBUTE_NAME.toString(),id));
        extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_MANAGED_SPECS_CONFIG.toString(),specificationsAttribute));
        extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_MANDATORY_ATTRIBUTE_NAME.toString(),String.valueOf(mandatory)));
        extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString(),selectedImplementation));
        extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_ALTERNATIVE_CONFIGURATION.toString(), extensionFactories.keySet().stream().collect(Collectors.joining(",", "{", "}"))));


        if (extension != null) {
            extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_IS_INSTANTIATE.toString(),"true"));

            InstanceDescription description = extension.getInstanceDescription();

            Element behaviorDescription = description.getDescription();
            extensionElement.addElement(behaviorDescription);
            elementToAttach.addElement(extensionElement);
        }else {
            extensionElement.addAttribute(new Attribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_IS_INSTANTIATE.toString(),"false"));
        }
    }


}
