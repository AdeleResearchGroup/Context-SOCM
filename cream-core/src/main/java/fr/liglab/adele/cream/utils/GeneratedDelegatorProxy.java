package fr.liglab.adele.cream.utils;

/**
 * Created by aygalinc on 09/09/16.
 */
public interface GeneratedDelegatorProxy {

    public Object getPojo();

    public void setPojo(Object pojo);

    public Object delegate(int methodhashcode, Object[] args) throws Throwable;
}
