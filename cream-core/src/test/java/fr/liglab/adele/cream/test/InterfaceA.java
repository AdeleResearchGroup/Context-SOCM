package fr.liglab.adele.cream.test;

/**
 * Created by aygalinc on 08/09/16.
 */
public interface InterfaceA {

    int RETURN = 10;

    int ERASE_RETURN = 20;

    public int methodInterfaceAImplementedInA();

    public int methodInterfaceAImplementedInB();

    default int methodInterfaceAImplementedByDefault(){
        return RETURN;
    }

    default int methodInterfaceAImplementedByDefaultButEraseByB(){
        return RETURN;
    }

}
