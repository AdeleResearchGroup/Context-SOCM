package fr.liglab.adele.cream.annotations.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import java.util.Map;
import java.util.Set;

/**
 * This interface groups all annotations useful to context entity provider
 */
public interface Creator {


    /**
     * Annotation to allow automatic injection of creator factories
     */
    @Target(ElementType.FIELD)
    public @interface Field {

        public static final String NO_PARAMETER = "";

		String value() default NO_PARAMETER;

	}


    /**
     * Annotation to allow using creators with dynamically created factories
     */
    @Target(ElementType.FIELD)
    public @interface Dynamic {
    }

    /**
     * A factory object used to create context entities of the specified type
     *
     * @param <E> The entity type
     */
    public interface Entity<E> {

        public void create(String id, Map<String, Object> initialization);

        public default void create(String id) {
        	create(id,null);
    	}

        public Set<String> identifiers();
        
        public E getInstance(String id);
        
        public String id(Object pojo);

        public void delete(String id);

        public default void deleteAll() {
        	for (String id : identifiers()) {
				delete(id);
			}
        }
    }

    /**
     * A factory object used to create relations between entities of the specified type
     *
     * @param <S> The source entity type
     * @param <T> The target entity type
     */
    public interface Relation<S, T> {

        public void link(S source, T target);

        public boolean isLinked(S source, T target);

        public void unlink(S source, T target);

        public void unlinkOutgoing(S source);

        public void unlinkIncoming(T target);

        
        public void link(String source, T target);

        public void link(S source, String target);

        public void link(String source, String target);


        public void unlink(String source, T target);

        public void unlink(S source, String target);

        public void unlink(String source, String target);

        
        public void unlinkOutgoing(String source);

        public void unlinkIncoming(String target);
        
        
        public boolean isLinked(String source, T target);

        public boolean isLinked(S source, String target);

        public boolean isLinked(String source, String target);


    }
}
