package fr.liglab.adele.cream.annotations.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface groups all annotations useful to context entity provider
 *
 */
public interface Creator {


	/**
	 * Annotation to allow automatic injection of creator factories
	 *
	 */
	@Target(ElementType.FIELD)
	public @interface Field {

		public static final String NO_PARAMETER = "";

		String value() default NO_PARAMETER;

		Class[] requirements() default {};
	}

	public interface Requirements{
		public Set<String> getRequirements();
	}

	/**
	 * A factory object used to create context entities of the specified type
	 *
	 * @param <E> The entity type
	 */
	public interface Entity<E> {

		public Set<String> getInstances();

		/**
		 * Return the created instance of the context entity
		 *
		 * TODO should return a Future because if the creator is disabled the actual instance
		 * may not be available
		 */
		public E getInstance(String id);

		/**
		 * Creates a new instance of the context entity
		 *
		 * TODO If there are errors at instantiation, and the creator is disabled how to notify
		 * the client?
		 */
		public void create(String id, Map<String, Object> initialization);

		public void create(String id);

		public void delete(String id);

		public void deleteAll();
	}

	/**
	 * A factory object used to create relations between entities of the specified type
	 *
	 * @param <S> The source entity type
	 * @param <T> The target entity type
	 */
	public interface Relation<S,T> {

		public Set<String> getInstances();

		public fr.liglab.adele.cream.model.Relation getInstance(String id);

		public List<fr.liglab.adele.cream.model.Relation> getInstancesRelatedTo(String sourceId);

		public List<fr.liglab.adele.cream.model.Relation> getInstancesRelatedTo(S source);

		public String create(S source, T target);

		public String create(String sourceId, String targetId);

		public String create(S source, String targetId);

		public String create(String sourceId, T target);

		public void delete(S source, T target);

		public void delete(String sourceId, String targetId);

		public void delete(S source, String targetId);

		public void delete(String sourceId, T target);
		
		void delete(String id);

		void deleteAll();

	}
}
