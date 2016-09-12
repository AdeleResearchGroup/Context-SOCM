package fr.liglab.adele.cream.utils;

import org.apache.felix.ipojo.Pojo;

import java.util.function.Supplier;

/**
 * Created by aygalinc on 12/09/16.
 */
public class Example implements GeneratedDelegatorProxy {

    private Object myPojo;

    @Override
    public void setPojo(Object pojo) {
    myPojo = pojo;
    }

    @Override
    public Object getPojo() {
        return myPojo;
    }

    @Override
    public Object delegate(int methodHash, Object[] args) throws Throwable {
        if (methodHash == 4533){
                 ((ContextServiceTe) myPojo).getS((Integer) args[0],(Byte) args[0],(Character) args[0], (Float) args[0],(Double) args[0],(Long) args[0], (Short) args[0], (Boolean) args[0] );
            return null;
        }
        return null;
    }


}
