package fr.liglab.adele.cream.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContextEntity {

	/**
	 * The list of provided context services
	 */
    Class<?> [] services();

    /**
     * The name of the service property used to describe context provider factories
     */
    public static final String ENTITY_CONTEXT_SERVICES = "factory.context.entity.services";

    /**
     * This annotations declares a relation of the context entity
     *
     */
    public interface Relation {
    	
    	public static String ID(Class<?> entity, String relation) {
    		return ID(entity.getSimpleName(),relation);
    	}

    	public static String ID(String entity, String relation) {
    		return entity.toLowerCase()+"."+relation;
    	}
    	
        @Target(ElementType.FIELD)
        public @interface Field {

        	public static final Class<?> DEFAULT_OWNER = Object.class;
        	
			/**
			 * The class of the service virtually owning the relation
			 * 
			 */
		    Class<?> owner() default Object.class;
		    
		    /**
	    	 * The name of the relation
	    	 */
        	String value();
        }
    }
    
	/**
	 * This interface groups all the annotations helping entities to implement context state
	 * 
	 */
	public interface State {
	
		public static String ID(Class<?> service, String state) {
			return ID(service.getSimpleName(),state);
		}
	
		public static String ID(String service, String state) {
			return service.toLowerCase()+"."+state;
		}
	
		@Target(ElementType.FIELD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Field {
			
			public static final String NO_VALUE = "";
	
			/**
			 * The class of the service defining the state to be implemented
			 * 
			 */
		    Class<?> service();
	
		    /**
		     * The name of the state to be implemented
		     */
		    String state();
		    
		    /**
		     * The default value of the state
		     */
		    String value() default NO_VALUE;
		    
		    /**
		     * Whether the service provider can modify the state using the injected field
		     */
		    boolean directAccess() default false;
		    
		}
		
		@Target(ElementType.FIELD)
		public @interface Apply {
	
			/**
			 * The class of the service defining the state to be implemented
			 * 
			 */
		    Class<?> service();
	
		    /**
		     * The name of the state to be implemented
		     */
		    String state();
		}
		
		@Target(ElementType.FIELD)
		public @interface Pull {
	
			/**
			 * The class of the service defining the state to be implemented
			 * 
			 */
		    Class<?> service();
	
		    /**
		     * The name of the state to be implemented
		     */
		    String state();
	
		    /**
		     * Sets the period of time.
		     */
		    long period() default -1L;
	
		    /**
		     * Sets the time unit to use for the period.
		     */
		    TimeUnit unit() default TimeUnit.SECONDS;
	
		}
		
		@Target(ElementType.METHOD)
		public @interface Push {
	
			/**
			 * The class of the service defining the state to be implemented
			 * 
			 */
		    Class<?> service();
	
		    /**
		     * The name of the state to be implemented
		     */
		    String state();
	
		}	
	
	}
    
}
