package fr.liglab.adele.cream.runtime.internal.proxies;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * A composite invocation handler that delegates invocations to the first handler able to handle the request 
 * 
 * @author vega
 *
 */
public class InvocationHandlerChain implements InvocationHandler {

	/**
	 * A marker interface to identity handlers that cooperate in the chain 
	 *
	 */
	public interface Link extends InvocationHandler {

		/**
		 * Invocation handlers can return this distinguished value to signal that they do not handle the requested method.
		 * 
		 * This avoids the overhead of throwing/catching exceptions in the case of cooperating handlers
		 */
	    public final static NoSuchMethodException NOT_SUCH_METHOD_EXCEPTION = new NoSuchMethodException();

	}
 
	
    /**
     * The list of delegates
     */
    private final Set<InvocationHandler> delegates = new ConcurrentSkipListSet<>();
    
     /**
     * Adds a new delegate to the end of the list
     */
    public void addDelegate(InvocationHandler delegate) {
    	delegates.add(delegate);
    }
    
    
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        for (InvocationHandler delegate : delegates) {
            	Object result = delegate.invoke(proxy, method, args);
            	if ( result != Link.NOT_SUCH_METHOD_EXCEPTION) {
            		return result;
            	}
        }
        
        throw new NoSuchMethodException(method.getName());
	}

}
