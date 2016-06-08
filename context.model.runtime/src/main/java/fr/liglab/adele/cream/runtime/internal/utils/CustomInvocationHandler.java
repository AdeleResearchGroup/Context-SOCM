package fr.liglab.adele.cream.runtime.internal.utils;

import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.apache.felix.ipojo.util.Callback;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aygalinc on 03/06/16.
 */
public class CustomInvocationHandler implements InvocationHandler {

    private final SuccessorStrategy mySuccessorStrategy;

    final private Object myPojo;

    private final Map<MethodIdentifier,Callback> myMethods = new HashMap<>();

    private final List<InvocationHandler> mySuccessor = new ArrayList<>();

    public CustomInvocationHandler(Object pojo, InstanceManager manager, SuccessorStrategy successorStrategy, List<InvocationHandler> successors){
        myPojo = pojo;
        mySuccessorStrategy = successorStrategy;
        mySuccessor.addAll(successors);
        PojoMetadata pojoMetadata = manager.getFactory().getPojoMetadata();

        MethodMetadata[] methodsMetadata = pojoMetadata.getMethods();
        for (MethodMetadata metadata: methodsMetadata){
            myMethods.put(new MethodIdentifier(pojoMetadata.getInterfaces(),metadata), new Callback(metadata,manager));
        }

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println(" Someone Calls ! " + method.getName() +  " : " + method.getDeclaringClass());
        for (Map.Entry<MethodIdentifier,Callback> entry : myMethods.entrySet()){
            if (entry.getKey().equals(method)){
                return entry.getValue().call(myPojo,args);
            }
        }

        return mySuccessorStrategy.successorStrategy(myPojo,mySuccessor,proxy,method,args);
    }

}
