package fr.liglab.adele.cream.test.invocation;

import fr.liglab.adele.cream.utils.MethodInvocationUtils;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.fest.assertions.Assertions.assertThat;

public class MethodUtilityTest {


    @Test
    public void testIsInvocableByReflexion() throws Throwable {

        ClassB b = new ClassB();
        Method m = b.getClass().getMethod("methodInterfaceAImplementedInB",null);
        assertThat(MethodInvocationUtils.isInvocableByReflexion(m,b)).isTrue();


        Method mA = ClassA.class.getMethod("methodInterfaceAImplementedInA",null);
        assertThat(MethodInvocationUtils.isInvocableByReflexion(mA,b)).isTrue();


        Method mAInterface = InterfaceA.class.getMethod("methodInterfaceAImplementedByDefault",null);
        assertThat(MethodInvocationUtils.isInvocableByReflexion(mAInterface,b)).isTrue();
    }


    @Test
    public void testInvocationByReflexion() throws Throwable {

        InterfaceA A = (InterfaceA) generateProxy();

        assertThat(A.methodInterfaceAImplementedInB()).isEqualTo(InterfaceA.RETURN);

        assertThat(A.methodInterfaceAImplementedInB()).isEqualTo(InterfaceA.RETURN);


        assertThat(A.methodInterfaceAImplementedByDefault()).isEqualTo(InterfaceA.RETURN);

        assertThat(A.methodInterfaceAImplementedByDefaultButEraseByB()).isEqualTo(InterfaceA.ERASE_RETURN);
    }


    public Object generateProxy(){
        Class[] clazz = new Class[1];
        clazz[0] = InterfaceA.class;

        return Proxy.newProxyInstance(ClassB.class.getClassLoader(),clazz,new BHandler());
    }

    public class BHandler implements InvocationHandler{

        Object myPojo = new ClassB();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (MethodInvocationUtils.isInvocableByReflexion(method,myPojo)){
                return  MethodInvocationUtils.invokeByReflexion(method,myPojo,proxy,args);
            }
            return null;
        }
    }




}
