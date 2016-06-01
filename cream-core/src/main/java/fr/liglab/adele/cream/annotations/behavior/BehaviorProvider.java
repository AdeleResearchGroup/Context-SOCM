package fr.liglab.adele.cream.annotations.behavior;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface BehaviorProvider {

    Class spec();
}
