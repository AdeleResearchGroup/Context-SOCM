package fr.liglab.adele.cream.runtime.internal.proxies;

import java.lang.reflect.Method;

import org.apache.felix.ipojo.Pojo;

import fr.liglab.adele.cream.runtime.internal.factories.EntityInstanceManager;
import fr.liglab.adele.cream.runtime.internal.proxies.dispatcher.MethodDispatcher;
import fr.liglab.adele.cream.runtime.internal.proxies.dispatcher.MethodDispatcherGenerator;

/**
 * An invocation handler that delegates every invocation to the pojo associated to a context entity
 * 
 * @author vega
 *
 */
public abstract class EntityInvocationHandler implements InvocationHandlerChain.Link {
	
	protected final EntityInstanceManager entity;
	
	protected EntityInvocationHandler(EntityInstanceManager entity) {
		this.entity = entity;
	}

	/**
	 * The handler in charge of methods declared in a provided service
	 */
	public static class Forservice extends EntityInvocationHandler {

		private final Class<?> 			service;
		private final MethodDispatcher	dispatcher;
		
		public Forservice(EntityInstanceManager entity, Class<?> service) {
			
			super(entity);

			this.service = service;
			
			MethodDispatcher dispatcher = null;
			try {
				dispatcher = MethodDispatcherGenerator.dispatcherFor(this.service); 
			} catch (Throwable e) {
			}
			
			this.dispatcher = dispatcher;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			
			if (dispatcher == null) {
				return NOT_SUCH_METHOD_EXCEPTION;
			}

			if (! method.getDeclaringClass().isAssignableFrom(service)) {
				return NOT_SUCH_METHOD_EXCEPTION;
			}

			Object pojo = entity.getPojoObject();
			
			if (! service.isInstance(pojo) ) {
				return NOT_SUCH_METHOD_EXCEPTION;
			}

			Object result = dispatcher.dispatch(pojo, method, args);
			
			return result == MethodDispatcher.UNKNOWN_METHOD ? NOT_SUCH_METHOD_EXCEPTION : result;

        }

	}

	/**
	 * The handler in charge of methods declared in Object
	 */
	public static class ForObject extends EntityInvocationHandler {

		public ForObject(EntityInstanceManager entity) {
			super(entity);
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			
			if (! Object.class.equals(method.getDeclaringClass())) {
				return NOT_SUCH_METHOD_EXCEPTION;
			}
			
			if ("equals".equals(method.getName()) && args.length == 1) {
				return proxy == args[0] || entity.getPojoObject().equals(args[0]);
			}

			if ("hashCode".equals(method.getName()) && (args == null || args.length == 0)) {
				return entity.getPojoObject().hashCode();
			}

			if ("toString".equals(method.getName()) && (args == null || args.length == 0)) {
				return entity.getPojoObject().toString();
			}

			return NOT_SUCH_METHOD_EXCEPTION;

		}
	}
	
	/**
	 * The handler in charge of methods declared in Pojo
	 */
	public static class ForPojo extends EntityInvocationHandler {

		public ForPojo(EntityInstanceManager entity) {
			super(entity);
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			
			if (! Pojo.class.equals(method.getDeclaringClass())) {
				return NOT_SUCH_METHOD_EXCEPTION;
			}
			
			Object pojo = entity.getPojoObject();
			
			if (! (pojo instanceof Pojo) ) {
				return NOT_SUCH_METHOD_EXCEPTION;
			}

			if ("getComponentInstance".equals(method.getName()) && (args == null || args.length == 0)) {
				return ((Pojo)pojo).getComponentInstance();
			}
			
			return NOT_SUCH_METHOD_EXCEPTION;

        }

	}

}
