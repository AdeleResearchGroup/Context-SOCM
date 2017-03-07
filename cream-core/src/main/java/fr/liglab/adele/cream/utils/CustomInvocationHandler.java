package fr.liglab.adele.cream.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by aygalinc on 03/06/16.
 */
public class CustomInvocationHandler implements InvocationHandler {

    private final SuccessorStrategy mySuccessorStrategy;

    private final Object myPojo;

    private final List<InvocationHandler> mySuccessor = new ArrayList<>();

    private final CreamGenerator myCreamGenerator;

    public CustomInvocationHandler(Object pojo, CreamGenerator manager, SuccessorStrategy successorStrategy, List<InvocationHandler> successors) {
        myPojo = pojo;
        mySuccessorStrategy = successorStrategy;
        mySuccessor.addAll(successors);
        myCreamGenerator = manager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<Method, GeneratedDelegatorProxy> mapOfDelegator = myCreamGenerator.getProxyDelegationMap();
        if (mapOfDelegator.containsKey(method)) {
            GeneratedDelegatorProxy generatedDelegatorProxy = mapOfDelegator.get(method);
            generatedDelegatorProxy.setPojo(myPojo);
            return generatedDelegatorProxy.delegate(method.hashCode(), args);
        }

        return mySuccessorStrategy.successorStrategy(myPojo, mySuccessor, proxy, method, args);
    }
}
