package fr.liglab.adele.cream.it.behavior.synchronisation;

/**
 * Created by aygalinc on 08/09/16.
 */
public interface BehaviorSpecToExtends {

    default boolean returnTrueDefaultMethod(){
        return true;
    }

    default boolean returnTrueDefaultMethodErase(){
        return true;
    }

    boolean returnFalse();
}
