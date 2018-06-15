package fr.liglab.adele.cream.runtime.handler.functional.extension.lifecycle;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.runtime.handler.entity.EntityStateHandler;
import fr.liglab.adele.cream.runtime.handler.functional.extension.tracker.FunctionalExtensionHandler;


@Handler(name = HandlerReference.FUNCTIONAL_EXTENSION_LIFECYCLE_HANDLER, namespace = HandlerReference.NAMESPACE)
public class FunctionalExtensionLifecyleHandler extends PrimitiveHandler implements ContextListener, InstanceStateListener, FunctionalExtensionHandler {

	private static final String QUALIFIED_ID = HandlerReference.NAMESPACE + ":" + HandlerReference.FUNCTIONAL_EXTENSION_LIFECYCLE_HANDLER;

    public static FunctionalExtensionLifecyleHandler forInstance(InstanceManager instance) {
        return instance != null ? (FunctionalExtensionLifecyleHandler) instance.getHandler(QUALIFIED_ID) : null;
    }

    private static final Logger LOG = LoggerFactory.getLogger(FunctionalExtensionLifecyleHandler.class);

    private ContextSource source;

    private final Map<String,Callback> stateCallbacks = new HashMap<>();

    private final Set<String> statesToListen = new HashSet<>();

    
    @Override
    public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {

        Element[] elements = metadata.getElements(HandlerReference.FUNCTIONAL_EXTENSION_LIFECYCLE_HANDLER, HandlerReference.NAMESPACE);
        if (elements == null || elements.length == 0) {
            return;
        }

        for (Element element : elements) {
            Element[] propertyElement = element.getElements();

            for (Element property : propertyElement) {
                String stateId = property.getAttribute("id");
                String methodCallbackId = property.getAttribute("method");
                
                MethodMetadata methodMetadata = select(methodCallbackId);

                if (methodMetadata == null) {
                    throw new ConfigurationException(" invalid callback (wrong number of arguments)" + methodCallbackId);
                }

                Callback methodCallBack = new Callback(methodMetadata, getInstanceManager());
                stateCallbacks.put(stateId,methodCallBack);
                statesToListen.add(stateId);
            }
        }
        
        getInstanceManager().addInstanceStateListener(this);

    }

    @Override
    public void attachCore(InstanceManager core) {
    	source = EntityStateHandler.forInstance(core);
    	source.registerContextListener(this,statesToListen.toArray(new String[statesToListen.size()]));
    } 

	@Override
	public void stateChanged(ComponentInstance extension, int extensiontState) {
		if (extensiontState == ComponentInstance.DISPOSED) {
			if (source != null) {
				source.unregisterContextListener(this);
				source = null;
			}
		}
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

    @Override
    public void update(ContextSource source, String property, Object value) {
       
    	if (getInstanceManager().getState() != ComponentInstance.VALID) {
            return;
        }
        
    	Callback callback = stateCallbacks.get(property);
    	
    	if (callback == null) {
            return;
        }
    	
    	String[] parameters = callback.getArguments();
        Object[] args 		= parameters.length == 0 ? null : new Object[] { value != null ? value : NULL_VALUE(parameters[0])};

        try {
        	callback.call(args);
        } catch (NoSuchMethodException e) {
            LOG.error("Error occurs during callback invocation of property : " + property + " cause by ", e);
        } catch (IllegalAccessException e) {
            LOG.error("Error occurs during callback invocation of property : " + property + " cause by ", e);
        } catch (InvocationTargetException e) {
            LOG.error("Error occurs during callback invocation of property : " + property + " cause by ", e);
        }
    }

    private final Object NULL_VALUE(String typeName) {
    	
		try {

			Class<?> type = getInstanceManager().getGlobalContext().getBundle().loadClass(typeName);
			
	    	if (!type.isPrimitive()) {
	    		return null;
	    	}
	    	
	    	if (Boolean.TYPE.equals(type))		{ return Boolean.FALSE;}
	    	if (Character.TYPE.equals(type))	{ return Character.valueOf('\u0000');}
	    	if (Byte.TYPE.equals(type))			{ return Byte.valueOf((byte)0);}
	    	if (Short.TYPE.equals(type))		{ return Short.valueOf((short)0);}
	    	if (Integer.TYPE.equals(type))		{ return Integer.valueOf(0);}
	    	if (Long.TYPE.equals(type))			{ return Long.valueOf(0L);}
	    	if (Float.TYPE.equals(type))		{ return Float.valueOf(0.0f);}
	    	if (Double.TYPE.equals(type))		{ return Double.valueOf(0.0d);}

	    	/*
	    	 * void type
	    	 */
	    	return null;

		} catch (ClassNotFoundException e) {
			return null;
		}
    }	
   
    /**
     * Selects the callback to invoke when there are many methods with the same name and different signatures
     */
    protected MethodMetadata select(String methodName) {
    	
    	MethodMetadata[] candidates = getPojoMetadata().getMethods(methodName);
    	MethodMetadata bestMatch 	= null;
    	for (MethodMetadata candidate : candidates) {
			
    		if (candidate.getMethodArguments().length > 1) {
    			continue;
			}
    		
    		if (bestMatch == null || bestMatch.getMethodArguments().length  < candidate.getMethodArguments().length) {
    			bestMatch = candidate;
    		}
		}
    	
    	return bestMatch;
    }
    
    

}
