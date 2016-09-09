package fr.liglab.adele.cream.test.invocation;

/**
 * Created by aygalinc on 08/09/16.
 */
public class ClassB extends ClassA{
    @Override
    public int methodInterfaceAImplementedInB() {
        return RETURN;
    }

    @Override
    public int methodInterfaceAImplementedByDefaultButEraseByB() {
        return ERASE_RETURN;
    }
}
