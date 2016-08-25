package fr.liglab.adele.cream.annotations.behavior;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectedBehavior {

    String id();

}
