package fr.liglab.adele.cream.annotations.functional.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FunctionalExtender {

    Class[] contextServices();

    @Target(ElementType.METHOD)
    @interface ChangeOn{
        Class contextService();

        String id();
    }
}
