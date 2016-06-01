package fr.liglab.adele.cream.annotations.internal;

/**
 * This class keeps the shared identifiers that allow to map a given annotation to a runtime handler
 * that handles it 
 * 
 * @author vega
 *
 */
public interface HandlerReference {

	/**
	 * The namespace associated to context handlers
	 */
	public static final String NAMESPACE = "fr.liglab.adele.cream.runtime.handler";

	/**
	 * The handler in charge of managing entities
	 */
	public static final String ENTITY_HANDLER = "entity";

	/**
	 * The handler in charge of managing relations
	 */
	public static final String RELATION_HANDLER = "relation";

	/**
	 * The handler in charge of managing creators
	 */
	public static final String CREATOR_HANDLER = "creation";

}
