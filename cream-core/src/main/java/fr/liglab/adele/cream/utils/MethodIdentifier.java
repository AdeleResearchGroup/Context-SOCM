package fr.liglab.adele.cream.utils;

import org.apache.felix.ipojo.parser.MethodMetadata;

import java.lang.reflect.Method;

/**
 * Created by aygalinc on 03/06/16.
 */
public class MethodIdentifier {

    final String[] interfaces;

    final String[] paramsType;

    final String methodName;

    final String methodReturnType;

    public MethodIdentifier(String[] interfaz,MethodMetadata methodMetadata) {
        interfaces = interfaz;
        paramsType = methodMetadata.getMethodArguments();
        methodName = methodMetadata.getMethodName();
        methodReturnType = methodMetadata.getMethodReturn();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Method){

            Method method = (Method) o;

            /** Check Method Declaring Class **/
            if (!equalDeclaringClass(method)){
                return false;
            }

            /** Check method name **/
            if(! methodName.equals(method.getName())){
                return false;
            }

            /** Check returnType name **/

            if (! methodReturnType.equals(method.getReturnType().getName())){
                return false;
            }

            if (!equalParamType(method)){
                return false;
            }

            return true;
        }
        if(o instanceof MethodIdentifier){
            return super.equals(o);
        }
        return false;
    }

    private boolean equalParamType(Method method){
        Class[] paramsClass = method.getParameterTypes();
        if (paramsClass.length != paramsType.length){
            return false;
        }

        for (int i = 0; i < paramsClass.length; i++) {
            if (paramsType[i].equals(paramsClass[i].getName())){
                continue;
            }
            return false;
        }
        return true;
    }

    private boolean equalDeclaringClass(Method method){
        for(String interfaz : interfaces ) {
            if (interfaz.equals(method.getDeclaringClass().getName())){
                return true;
            }
        }
        return false;
    }

}
