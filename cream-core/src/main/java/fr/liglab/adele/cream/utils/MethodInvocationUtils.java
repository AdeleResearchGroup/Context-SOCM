package fr.liglab.adele.cream.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodInvocationUtils {

    public static boolean isInvocableByReflexion(Method method,Object myPojo){
        final Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.isInstance(myPojo)){
           return true;
        }
        return false;
    }

    public static Object invokeByReflexion(Method method,Object myPojo,Object proxy,Object[] args) throws Throwable {
        final Class<?> declaringClass = method.getDeclaringClass();
        if (isInvocableByReflexion(method, myPojo)){
            final MethodHandles.Lookup lookupMyPojo = MethodHandles.publicLookup().in(myPojo.getClass());
            MethodHandle handle = lookupMyPojo.unreflect(method);
            if (handle != null){
                final MethodHandles.Lookup lookup = MethodHandles.publicLookup().in(myPojo.getClass());

                return lookup.unreflect(method)
                        .bindTo(myPojo)
                        .invokeWithArguments(args);
            }
        }

        throw new CreamInvocationException();
    }
}
