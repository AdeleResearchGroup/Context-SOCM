package fr.liglab.adele.cream.utils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by aygalinc on 13/09/16.
 */
@FunctionalInterface
public interface CreamGenerator {

    public Map<Method,GeneratedDelegatorProxy> getProxyDelegationMap();
}
