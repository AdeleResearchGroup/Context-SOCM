package fr.liglab.adele.cream.it.behavior.synchronisation;

/**
 * Created by aygalinc on 08/09/16.
 */
public abstract class AbstractBehaviorSpecToExtends implements BehaviorSpecToExtends{

    @Override
    public boolean returnTrueDefaultMethodErase() {
        return false;
    }

    @Override
    public boolean returnFalse() {
        return false;
    }
}
