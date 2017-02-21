package fr.liglab.adele.cream.it.functional.extension.synchronisation;

/**
 * Created by aygalinc on 08/09/16.
 */
public interface ExtensionSpecToExtends {

    default boolean returnTrueDefaultMethod() {
        return true;
    }

    default boolean returnTrueDefaultMethodErase() {
        return true;
    }

    boolean returnFalse();
}
