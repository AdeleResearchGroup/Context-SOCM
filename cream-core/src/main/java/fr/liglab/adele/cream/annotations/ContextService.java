package fr.liglab.adele.cream.annotations;

import java.lang.annotation.*;

/**
 * Created by aygalinc on 14/01/16.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ContextService {

    public static final String DEFAULT_VALUE = "{ClassName.toLowerCase()}";

    /**
     * The name of the service.
     * <p>
     * If not specified it will be extracted from the annotated interface
     */
    String value() default DEFAULT_VALUE;

}
