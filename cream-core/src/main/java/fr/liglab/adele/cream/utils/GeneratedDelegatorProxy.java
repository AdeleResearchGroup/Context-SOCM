package fr.liglab.adele.cream.utils;

/**
 * Created by aygalinc on 09/09/16.
 */
public interface GeneratedDelegatorProxy {

    public void setPojo(Object pojo);

    public Object getPojo();

    public Object delegate(int methodhashcode,Object[] args) throws Throwable;
}
