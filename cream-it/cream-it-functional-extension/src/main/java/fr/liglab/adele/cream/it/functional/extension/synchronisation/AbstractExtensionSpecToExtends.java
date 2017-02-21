package fr.liglab.adele.cream.it.functional.extension.synchronisation;

/**
 * Created by aygalinc on 08/09/16.
 */
public abstract class AbstractExtensionSpecToExtends implements ExtensionSpecToExtends {

    @Override
    public boolean returnTrueDefaultMethodErase() {
        return false;
    }

    @Override
    public boolean returnFalse() {
        return false;
    }
}
