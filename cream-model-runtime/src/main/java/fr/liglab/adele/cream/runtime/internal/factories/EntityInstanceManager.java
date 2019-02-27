package fr.liglab.adele.cream.runtime.internal.factories;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.InstanceManager;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.cream.annotations.internal.EntityInstance;
import fr.liglab.adele.cream.runtime.internal.proxies.EntityInvocationHandler;
import fr.liglab.adele.cream.runtime.internal.proxies.InvocationHandlerChain;

/**
 * This class handles the common behavior of core and extension context entities
 * 
 * @author vega
 *
 */
public abstract class EntityInstanceManager extends InstanceManager implements EntityInstance {


    protected EntityInstanceManager(ComponentFactory factory, BundleContext context, HandlerManager[] handlers) {
        super(factory, context, handlers);
    }

 
    /**
     * The list of services provided by this entity
     */
    public abstract Collection<Class<?>> getEntityServices();

    
	/**
	 * The invocation handler used to delegate method invocation to the pojo associated to the instance
	 */
    protected InvocationHandlerChain invocationHanlder;

	/**
	 * Builds a chain of invocation handlers to delegate the method invocation of each provided service of the
	 * entity 
	 */
	public InvocationHandlerChain getDelegationHandler() {
		if (invocationHanlder == null) {
			
			invocationHanlder = new InvocationHandlerChain();
			invocationHanlder.addDelegate(new EntityInvocationHandler.ForObject(this));
			invocationHanlder.addDelegate(new EntityInvocationHandler.ForPojo(this));

			for (Class<?> service : flatten(getEntityServices())) {
				invocationHanlder.addDelegate(new EntityInvocationHandler.Forservice(this, service));
			}
		}
		
		return invocationHanlder;
	}

    /**
     * Takes a list of classes and produces a set without duplicates that keeps only the most specialized classes
     * in the hierarchy
     */
    private static Set<Class<?>> flatten(Collection<Class<?>> classes) {
        
    	Set<Class<?>> result = new HashSet<>();

        for (Class<?> candidate : classes) {
        	
        	boolean candidateIsLeaf 					= true;
            Collection<Class<?>> ancestorsOfCandidate 	= new ArrayList<>();
            
            for (Class<?> previousCandidate : result) {
                
            	if (candidate.isAssignableFrom(previousCandidate)) {
            		candidateIsLeaf = false;
                }
                
            	if (previousCandidate.isAssignableFrom(candidate) && !(previousCandidate.equals(candidate))) {
            		ancestorsOfCandidate.add(previousCandidate);
                }
            }

            if (candidateIsLeaf) {
            	result.add(candidate);
            }
            
            result.removeAll(ancestorsOfCandidate);
        }

        return result;
    }
    


 

}
