package fr.liglab.adele.cream.utils;

import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.Pojo;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.apache.felix.ipojo.util.Callback;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by aygalinc on 03/06/16.
 */
public class CustomInvocationHandler implements InvocationHandler {

    private final SuccessorStrategy mySuccessorStrategy;

    private final  Object myPojo;

    private final Map<MethodIdentifier,Callback> myMethods = new HashMap<>();

    private final List<InvocationHandler> mySuccessor = new ArrayList<>();

    public CustomInvocationHandler(Object pojo, InstanceManager manager, SuccessorStrategy successorStrategy, List<InvocationHandler> successors){
        myPojo = pojo;
        mySuccessorStrategy = successorStrategy;
        mySuccessor.addAll(successors);
        PojoMetadata pojoMetadata = manager.getFactory().getPojoMetadata();

        MethodMetadata[] methodsMetadata = pojoMetadata.getMethods();
        for (MethodMetadata metadata: methodsMetadata){
                final String[] listOfInter = getAllInterface(pojo);
                myMethods.put(new MethodIdentifier(listOfInter,metadata), new Callback(metadata,manager));
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        for (Map.Entry<MethodIdentifier,Callback> entry : myMethods.entrySet()){
            if (entry.getKey().equals(method)){
                return entry.getValue().call(myPojo,args);
            }
        }

        return mySuccessorStrategy.successorStrategy(myPojo,mySuccessor,proxy,method,args);
    }

    private String[] getAllInterface(Object pojoType){
        Set<String> listOfInterfaces = new HashSet<>();
        recursiveIntrospection(listOfInterfaces,pojoType.getClass());
       return listOfInterfaces.toArray(new String[listOfInterfaces.size()]);
    }

    private void recursiveIntrospection(Set<String> returnSet,Class pojoType) {

        if (pojoType.isInterface()){
            returnSet.add(pojoType.getName());
        }
        Class[] interfaz = pojoType.getInterfaces();
        if (interfaz == null){
            return;
        }
        for (Class clazz:interfaz){
            if (!clazz.equals(Pojo.class))
            recursiveIntrospection(returnSet,clazz);
        }
        return;
    }
}
