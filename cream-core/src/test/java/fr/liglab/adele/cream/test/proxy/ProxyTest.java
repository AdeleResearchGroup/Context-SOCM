package fr.liglab.adele.cream.test.proxy;

import fr.liglab.adele.cream.utils.CreamProxyGenerator;
import fr.liglab.adele.cream.utils.GeneratedDelegatorProxy;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.fail;

public class ProxyTest {


    @Test
    public void testProxyGetterAndSetter() throws Throwable {

       ProxyFactory proxyFactory = new ProxyFactory(ServiceH.class.getClassLoader());
        Object generatedProxy = proxyFactory.getProxyInstance(ServiceH.class,pojo.class,"coincoin-52");

        if (!(generatedProxy instanceof GeneratedDelegatorProxy)){
            fail("Proxy is not an instance of  GeneratedDelegatorProxy");
        }
        pojo myPojo = new pojo();
        GeneratedDelegatorProxy proxy = (GeneratedDelegatorProxy) generatedProxy;
        proxy.setPojo(myPojo);

        for (Method method: ServiceH.class.getMethods()){
            Class clazz = method.getDeclaringClass();
            proxy.delegate(method.hashCode(),null);
        }

    }

    private class ProxyFactory extends ClassLoader{

        public ProxyFactory(ClassLoader parent){
            super(parent);
        }

        private Class getProxyClass(Class specToDelegate,Class pojoClass,String InstanceManagerId){
            byte[] clz = CreamProxyGenerator.dump(specToDelegate,pojoClass,InstanceManagerId); // Generate the proxy.
            // Turn around the VM changes (FELIX-2716) about java.* classes.
            String cn = pojoClass.getName();
            if (cn.startsWith("java.")) {
                cn = "$" + cn;
            }
            return defineClass(cn + "$$Proxy"+InstanceManagerId.hashCode(), clz, 0, clz.length);
        }

        private Object getProxyInstance(Class specToDelegate,Class pojoClass,String InstanceManagerId ){
            try {
                Class clazz = getProxyClass(specToDelegate,pojoClass,InstanceManagerId);
                Constructor constructor = clazz.getConstructor();
                return constructor.newInstance();
            } catch (Throwable e) {
                fail("Failed to instantiate a proxy" + e.toString());
                return null;
            }
        }

    }





}
