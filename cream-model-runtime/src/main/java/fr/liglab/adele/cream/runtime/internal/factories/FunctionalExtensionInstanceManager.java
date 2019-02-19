package fr.liglab.adele.cream.runtime.internal.factories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.HandlerManager;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

/**
 * Created by aygalinc on 31/05/16.
 */
public class FunctionalExtensionInstanceManager extends EntityInstanceManager {

    /**
     * Creates a new Component Manager.
     * The instance is not initialized.
     *
     * @param factory  the factory managing the instance manager
     * @param context  the bundle context to give to the instance
     * @param handlers handler object array
     */
    public FunctionalExtensionInstanceManager(ComponentFactory factory, BundleContext context, HandlerManager[] handlers) {
        super(factory, context, handlers);
    }


 	@Override
	public Collection<Class<?>> getEntityServices() {
 		
		List<Class<?>> services 	= new ArrayList<>();
        Class<?> implementation 	= getClazz();
        
        FunctionalExtender entityDeclaration =  implementation.getAnnotation(FunctionalExtender.class);
        
        if (entityDeclaration != null) {
            services.addAll(Arrays.asList(entityDeclaration.contextServices()));
        }

		return services;

	}

}
