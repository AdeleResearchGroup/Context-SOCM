package fr.liglab.adele.cream.annotations.functional.extension;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FunctionalExtensions.class)
public @interface FunctionalExtension {

    Class[] contextServices();

    Class implementation();

    String id();

    boolean mandatory() default false;
}
