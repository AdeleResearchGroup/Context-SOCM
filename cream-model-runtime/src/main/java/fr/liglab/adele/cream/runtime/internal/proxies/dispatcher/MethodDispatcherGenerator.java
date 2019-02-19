package fr.liglab.adele.cream.runtime.internal.proxies.dispatcher;

import org.objectweb.asm.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by aygalinc on 09/09/16.
 */
public class MethodDispatcherGenerator implements Opcodes {



    private MethodDispatcherGenerator() {
        //To hide the public constructor
    }

    /**
     * Generates a new dispatcher class for a given specification
     */
    public static Class<MethodDispatcher> generate(ClassLoader loader, Class<?> specification) throws ClassNotFoundException {
    	assert specification.isInterface();
    	return new Loader(loader,specification).getDispatcherClass();
    } 

    public static Class<MethodDispatcher> generate(Class<?> specification) throws ClassNotFoundException {
    	return generate(specification.getClassLoader(), specification);
    }
    
    /**
     * Instantiate the dispatcher class
     */

    public static MethodDispatcher dispatcherFor(ClassLoader loader, Class<?> specification)  {
        
		try {
	        return instantiate(generate(loader,specification));
		} catch (ReflectiveOperationException e) {
			return null;
		}
    }

    public static MethodDispatcher dispatcherFor(Class<?> specification)  {
			return dispatcherFor(specification.getClassLoader(),specification);
    }

    private static MethodDispatcher instantiate(Class<MethodDispatcher> dispatcher) throws ReflectiveOperationException  {
			Constructor<MethodDispatcher> constructor = dispatcher.getConstructor();
	        return constructor.newInstance();
    }


    /**
     * A class loader that is able to dynamically load the dispatcher class of a given specification.
     * 
     * For resolving referenced classes of the generated  class we delegate to the loader of the specification
     * or our own loader for internal classes
     * 
     * @author vega
     *
     */
    private static class Loader extends ClassLoader {

    	private final Class<?> 		specification;

        public Loader(ClassLoader parent, Class<?> specification) {
            super(parent);
            this.specification = specification;
        }


		@SuppressWarnings("unchecked")
		public Class<MethodDispatcher> getDispatcherClass() throws ClassNotFoundException {
        	return (Class<MethodDispatcher>) loadClass(getDispatcherName(specification));
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
        	
        	if (isDispatcher(name)) {
                byte[] bytecode = generateBytecode(specification,name.replace('.','/'));
                return defineClass(name, bytecode, 0, bytecode.length);
        	}
        	
        	return super.findClass(name);
        }
        
        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        	
        	if (MethodDispatcher.class.getCanonicalName().equals(name)) {
        		return MethodDispatcher.class;
        	}
        	
        	return super.loadClass(name, resolve);
        }
        
        /**
         *The naming conventions for the generated class
         */
        
        private static String getDispatcherName(Class<?> specification) {
        	return "cream.generated.dispatchers."+specification.getName();
        }

        private static boolean isDispatcher(String className) {
        	return className.startsWith("cream.generated.dispatchers");
        }
        
    }


    /**
     * The bytecode genertaor
     */
    

    private static final String OBJECT 			= Type.getInternalName(Object.class);
    private static final String DISPATCHER 		= Type.getInternalName(MethodDispatcher.class);
    

    private static byte[] generateBytecode(Class<?> specification, String classname) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS); // use compute max to automatically compute the max stack size and the max of local variables of a method

        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, classname, null, OBJECT, new String[] {DISPATCHER});

        init(classWriter);
        dispatch(classWriter,specification);

        classWriter.visitEnd();

        return classWriter.toByteArray();

    }

    private static void init(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);

        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);

        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0);

        mv.visitEnd();

    }

    private static void dispatch(ClassWriter cw, Class<?> specification) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "dispatch", "(Ljava/lang/Object;I[Ljava/lang/Object;)Ljava/lang/Object;", null, new String[]{"java/lang/Throwable"});

        Method[] methods 	= specification.getMethods(); // used to generate method
        Label[]  next		= new Label [methods.length];


        for (int i = 0; i < next.length; i++) {
        	next[i] = new Label();
        }
        	
        mv.visitCode();

        for (int i = 0; i < methods.length; i++) {
        	Method method = methods[i];
        	
            mv.visitLdcInsn(MethodDispatcher.id(method)); // create an int const, used in the next if
            mv.visitVarInsn(ILOAD, 2); //Load second argument : the int hashcode methode name
            mv.visitJumpInsn(IF_ICMPNE, next[i]); // jump to next method label

            generateDelegator(mv, specification, method); // generate the delegator part

            mv.visitLabel(next[i]);
            mv.visitFrame(F_SAME, 0, null, 0, null);

        }

        mv.visitLdcInsn(MethodDispatcher.UNKNOWN_METHOD);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        return;
    }

    private static void generateDelegator(MethodVisitor mv, Class<?> specification, Method method) {
        org.objectweb.asm.commons.Method asmMethod = org.objectweb.asm.commons.Method.getMethod(method);
        String interfaceName = Type.getInternalName(specification);
        mv.visitVarInsn(ALOAD, 1); //Load first argument : the ipojo object
        mv.visitTypeInsn(CHECKCAST, interfaceName);

        Class<?>  parametersClass [] = method.getParameterTypes();
        int i = 0;

        for (Class<?> paramClass : parametersClass) {
            mv.visitVarInsn(ALOAD, 3);
            pushArgsOnStack(mv, i);
            mv.visitInsn(AALOAD);
            checkCast(mv, paramClass);
            i++;
        }


        mv.visitMethodInsn(INVOKEINTERFACE, interfaceName, asmMethod.getName(), asmMethod.getDescriptor(), true);

        if (method.getReturnType().equals(Void.TYPE)) { // Void Return Case
            mv.visitInsn(ACONST_NULL);
        } else {
            boxReturn(mv, method.getReturnType());
        }

        mv.visitInsn(ARETURN);
    }

    private static void pushArgsOnStack(MethodVisitor mv, int i) {
        if (i == 0) {
            mv.visitInsn(ICONST_0);
        } else if (i == 1) {
            mv.visitInsn(ICONST_1);
        } else if (i == 2) {
            mv.visitInsn(ICONST_2);
        } else if (i == 3) {
            mv.visitInsn(ICONST_3);
        } else if (i == 4) {
            mv.visitInsn(ICONST_4);
        } else if (i == 5) {
            mv.visitInsn(ICONST_5);
        } else {
            mv.visitIntInsn(BIPUSH, i);
        }

    }

    /**
     * Boxing/unboxing arguments and result
     */
    private static final String INTEGER 		= Type.getInternalName(Integer.class);;
    private static final String BOOLEAN 		= Type.getInternalName(Boolean.class);
    private static final String BYTE 			= Type.getInternalName(Byte.class);
    private static final String DOUBLE			= Type.getInternalName(Double.class);
    private static final String FLOAT 			= Type.getInternalName(Float.class);
    private static final String LONG 			= Type.getInternalName(Long.class);
    private static final String SHORT 			= Type.getInternalName(Short.class);
    private static final String CHARACTER 		= Type.getInternalName(Character.class);

    private static final String UNBOX_METHOD 	= "valueOf";

    private static void checkCast(MethodVisitor mv, Class<?> paramClass) {
        Type paramType = Type.getType(paramClass);
        if (Type.INT_TYPE.equals(paramType)) {
            mv.visitTypeInsn(CHECKCAST, INTEGER);
            mv.visitMethodInsn(INVOKEVIRTUAL, INTEGER, "intValue", "()I", false);
        } else if (Type.BOOLEAN_TYPE.equals(paramType)) {
            mv.visitTypeInsn(CHECKCAST, BOOLEAN);
            mv.visitMethodInsn(INVOKEVIRTUAL, BOOLEAN, "booleanValue", "()Z", false);
        } else if (Type.BYTE_TYPE.equals(paramType)) {
            mv.visitTypeInsn(CHECKCAST, BYTE);
            mv.visitMethodInsn(INVOKEVIRTUAL, BYTE, "byteValue", "()B", false);
        } else if (Type.CHAR_TYPE.equals(paramType)) {
            mv.visitTypeInsn(CHECKCAST, CHARACTER);
            mv.visitMethodInsn(INVOKEVIRTUAL, CHARACTER, "charValue", "()C", false);
        } else if (Type.DOUBLE_TYPE.equals(paramType)) {
            mv.visitTypeInsn(CHECKCAST, DOUBLE);
            mv.visitMethodInsn(INVOKEVIRTUAL, DOUBLE, "doubleValue", "()D", false);
        } else if (Type.FLOAT_TYPE.equals(paramType)) {
            mv.visitTypeInsn(CHECKCAST, FLOAT);
            mv.visitMethodInsn(INVOKEVIRTUAL, FLOAT, "floatValue", "()F", false);
        } else if (Type.LONG_TYPE.equals(paramType)) {
            mv.visitTypeInsn(CHECKCAST, LONG);
            mv.visitMethodInsn(INVOKEVIRTUAL, LONG, "longValue", "()J", false);
        } else if (Type.SHORT_TYPE.equals(paramType)) {
            mv.visitTypeInsn(CHECKCAST, SHORT);
            mv.visitMethodInsn(INVOKEVIRTUAL, SHORT, "shortValue", "()S", false);
        } else {
            mv.visitTypeInsn(CHECKCAST, paramType.getInternalName());
        }
    }

    private static void boxReturn(MethodVisitor mv, Class<?> paramClass) {
        Type paramType = Type.getType(paramClass);
        if (Type.INT_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, INTEGER, UNBOX_METHOD, "(I)Ljava/lang/Integer;", false);
        } else if (Type.BOOLEAN_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, BOOLEAN, UNBOX_METHOD, "(Z)Ljava/lang/Boolean;", false);
        } else if (Type.BYTE_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, BYTE, UNBOX_METHOD, "(B)Ljava/lang/Byte;", false);
        } else if (Type.CHAR_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, CHARACTER, UNBOX_METHOD, "(C)Ljava/lang/Character;", false);
        } else if (Type.DOUBLE_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, DOUBLE, UNBOX_METHOD, "(D)Ljava/lang/Double;", false);
        } else if (Type.FLOAT_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, FLOAT, UNBOX_METHOD, "(F)Ljava/lang/Float;", false);
        } else if (Type.LONG_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, LONG, UNBOX_METHOD, "(J)Ljava/lang/Long;", false);
        } else if (Type.SHORT_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, SHORT, UNBOX_METHOD, "(S)Ljava/lang/Short;", false);
        }
    }


}

