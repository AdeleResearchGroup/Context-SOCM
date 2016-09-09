package fr.liglab.adele.cream.utils;

import java.lang.reflect.Method;

/**
 * Created by aygalinc on 09/09/16.
 */
public interface GeneratedDelegatorProxy {

    public void setPojo(Object pojo);
// mv = cw.visitMethod(ACC_PUBLIC, "setPojo", "(Ljava/lang/Object;)V", null, null);

    public Object getPojo();
// mv = cw.visitMethod(ACC_PUBLIC, "setPojo", "(Ljava/lang/Object;)V", null, null);

    public Object delegate(Method method,Object[] args) throws Throwable;
   // mv = cw.visitMethod(ACC_PUBLIC, "delegate", "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
}
