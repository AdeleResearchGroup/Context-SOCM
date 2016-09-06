package fr.liglab.adele.cream.annotations.behavior;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Behaviors.class)
public @interface Behavior {

    Class spec();

    Class implem();

    String id();

    boolean mandatory() default false;
}
