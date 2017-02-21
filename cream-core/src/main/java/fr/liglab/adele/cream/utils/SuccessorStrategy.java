package fr.liglab.adele.cream.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

@FunctionalInterface
public interface SuccessorStrategy {

    final String NO_FOUND_CODE = "NOT_FOUND_METHOD_IN_CLASS#404";

    public Object successorStrategy(Object pojo, List<InvocationHandler> successors, Object proxy, Method method, Object[] args);
}
