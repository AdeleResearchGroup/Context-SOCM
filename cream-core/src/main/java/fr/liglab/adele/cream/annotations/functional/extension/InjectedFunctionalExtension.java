package fr.liglab.adele.cream.annotations.functional.extension;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectedFunctionalExtension {

    String id();

}
