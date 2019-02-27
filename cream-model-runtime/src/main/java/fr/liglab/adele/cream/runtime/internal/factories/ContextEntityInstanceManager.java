package fr.liglab.adele.cream.runtime.internal.factories;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.felix.ipojo.ComponentFactory;
import org.osgi.framework.BundleContext;
import org.apache.felix.ipojo.HandlerManager;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;


import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.runtime.handler.functional.extension.tracker.FunctionalExtensionTrackerHandler;
import fr.liglab.adele.cream.runtime.internal.proxies.InvocationHandlerChain;

/**
 * Created by aygalinc on 31/05/16.
 */
public class ContextEntityInstanceManager extends EntityInstanceManager  {

    /**
     * Creates a new Component Manager.
     * The instance is not initialized.
     *
     * @param factory  the factory managing the instance manager
     * @param context  the bundle context to give to the instance
     * @param handlers handler object array
     */
    public ContextEntityInstanceManager(ComponentFactory factory, BundleContext context, HandlerManager[] handlers) {
        super(factory, context, handlers);

    }

	@Override
	public Collection<Class<?>> getEntityServices() {
		
		List<Class<?>> services 	= new ArrayList<>();
        Class<?> implementation 	= getClazz();
        
        ContextEntity entityDeclaration =  implementation.getAnnotation(ContextEntity.class);
        
        if (entityDeclaration != null) {
            services.addAll(Arrays.asList(entityDeclaration.coreServices()));
        }

		return services;
	}

	@Override
	public InvocationHandlerChain getDelegationHandler() {

		if (invocationHanlder == null) {
			invocationHanlder= super.getDelegationHandler(); 
			invocationHanlder.addDelegate(getExtensionTrcakerHandler());
		}
		
		return invocationHanlder;
	}
    
	private InvocationHandler getExtensionTrcakerHandler() {
        Object tracker = this.getHandler(HandlerReference.NAMESPACE + ":" + HandlerReference.FUNCTIONAL_EXTENSION_TRACKER_HANDLER);
        return  tracker != null ? ((FunctionalExtensionTrackerHandler) tracker).getDelegationHandler() : null;
    }

}
