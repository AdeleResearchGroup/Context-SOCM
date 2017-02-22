package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.utils.CreamProxyFactory;
import fr.liglab.adele.cream.utils.GeneratedDelegatorProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by aygalinc on 20/02/17.
 */
public class ProxyGeneratorUtils {

    private ProxyGeneratorUtils(){
    }
    
    public static Map<Method, GeneratedDelegatorProxy> getGeneratedProxyByMethodMap(Set<Class> services, CreamProxyFactory creamProxyFactory) {

        Map<Method, GeneratedDelegatorProxy> proxyDelegatorMap = new HashMap<>();

        if (services == null) {
            return proxyDelegatorMap;
        }

        for (Class service : services) {
            Method[] methods = service.getMethods();
            if ((methods == null) || (methods.length == 0)) {
                break;
            }
            GeneratedDelegatorProxy proxy = (GeneratedDelegatorProxy) creamProxyFactory.getProxy(service);
            for (Method method : methods) {
                proxyDelegatorMap.put(method, proxy);
            }
        }
        return proxyDelegatorMap;
    }
}
