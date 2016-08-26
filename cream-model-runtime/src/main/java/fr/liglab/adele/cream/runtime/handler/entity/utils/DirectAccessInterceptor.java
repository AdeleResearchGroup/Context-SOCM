package fr.liglab.adele.cream.runtime.handler.entity.utils;

import org.apache.felix.ipojo.FieldInterceptor;

/**
 * Interceptor to handle state fields that are directly manipulated by the entity code
 */
public class DirectAccessInterceptor extends AbstractStateInterceptor implements StateInterceptor, FieldInterceptor {

	/**
	 * The associated entity handler in charge of keeping the context state
	 */
	private final AbstractContextHandler abstractContextHandler;

	/**
	 * @param abstractContextHandler
	 */
	public DirectAccessInterceptor(AbstractContextHandler abstractContextHandler) {
		this.abstractContextHandler = abstractContextHandler;
	}

	@Override
	public Object onGet(Object pojo, String fieldName, Object value) {
		return abstractContextHandler.getStateValue(fieldToState.get(fieldName));
	}

	@Override
	public void onSet(Object pojo, String fieldName, Object value) {
		abstractContextHandler.update(fieldToState.get(fieldName),value);
	}

	@Override
	public void validate() {
		//Do nothing
	}

	@Override
	public void invalidate() {
		//Do nothing
	}

}