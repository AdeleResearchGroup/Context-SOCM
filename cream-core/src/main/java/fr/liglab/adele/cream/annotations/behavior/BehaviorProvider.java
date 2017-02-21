package fr.liglab.adele.cream.annotations.behavior;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BehaviorProvider {

    Class[] spec();

    @Target(ElementType.METHOD)
    @interface ChangeOn{
        Class spec();

        String id();
    }
}
