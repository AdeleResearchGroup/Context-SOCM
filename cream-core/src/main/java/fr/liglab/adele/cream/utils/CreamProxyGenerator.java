package fr.liglab.adele.cream.utils;

import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aygalinc on 09/09/16.
 */
public class CreamProxyGenerator implements Opcodes {

    private static final String POJO = "myPojo";

    private static final String POJO_TYPE = Type.getDescriptor(Object.class);

    private static final String INTEGER = "java/lang/Integer";
    private static final String BOOLEAN = "java/lang/Boolean";
    private static final String BYTE = "java/lang/Byte";
    private static final String CHARACTER = "java/lang/Character";
    private static final String DOUBLE = "java/lang/Double";
    private static final String FLOAT = "java/lang/Float";
    private static final String LONG = "java/lang/Long";
    private static final String SHORT = "java/lang/Short";
    private static final String VALUE_OF_METHOD = "valueOf";

    private CreamProxyGenerator() {
        //To hide the public constructor
    }

    public static byte[] dump(Class spec, String uniqueId) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS); // use compute max to automaticali compute the max stack size and the max of local variables of a method

        String specName = Type.getInternalName(GeneratedDelegatorProxy.class);


        String[] itfs = new String[]{specName};
        String parent = "java/lang/Object";

        String className = Type.getInternalName(spec) + "$$Proxy" + uniqueId.hashCode(); // Unique name.
        if (className.startsWith("java/")) {
            className = "$" + className;
        }

        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, className, null, parent, itfs);

        addPojoField(classWriter);
        generateConstructor(classWriter);
        addGetterMethod(classWriter, className);
        addSetterMethod(classWriter, className);
        addCallerMethod(classWriter, className, spec);

        classWriter.visitEnd();

        return classWriter.toByteArray();

    }

    private static void addPojoField(ClassWriter cw) {
        FieldVisitor fieldVisitor = cw.visitField(Opcodes.ACC_PRIVATE, POJO, POJO_TYPE, null, null);
        fieldVisitor.visitEnd();
    }

    private static void generateConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);

        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);

        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0);

        mv.visitEnd();

    }

    private static void addSetterMethod(ClassWriter cw, String className) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "setPojo", "(" + POJO_TYPE + ")V", null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0); //Load this

        mv.visitVarInsn(ALOAD, 1); // Load the argument

        mv.visitFieldInsn(PUTFIELD, className, POJO, POJO_TYPE); // put argument on pojo field

        mv.visitInsn(RETURN); // return void

        mv.visitMaxs(0, 0);

        mv.visitEnd();


    }

    private static void addGetterMethod(ClassWriter cw, String className) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getPojo", "()" + POJO_TYPE, null, null);

        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0); //Load this

        mv.visitFieldInsn(GETFIELD, className, POJO, POJO_TYPE); // Get the myPojo field type Object

        mv.visitInsn(ARETURN);// return pojo

        mv.visitMaxs(0, 0);

        mv.visitEnd(); // visit end
    }

    private static void addCallerMethod(ClassWriter cw, String className, Class specToDelegate) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "delegate", "(I[Ljava/lang/Object;)Ljava/lang/Object;", null, new String[]{"java/lang/Throwable"});

        Method[] methods = specToDelegate.getMethods(); // used to generate method
        int numberOfMethod = methods.length;

        Map<Integer, Method> mapPositionToMthod = new HashMap<>();
        Map<Integer, Label> mapLabelToJump = new HashMap<>();
        Map<Integer, Label> mapLabelToVisit = new HashMap<>();

        int i = 0;
        for (Method method : methods) {
            Label label = new Label();

            mapLabelToJump.put(i, label);
            mapLabelToVisit.put(i + 1, label);

            mapPositionToMthod.put(i, method);
            i++;
        }

        mv.visitCode();

        for (int j = 0; j < numberOfMethod; j++) {
            Method method = mapPositionToMthod.get(j);

            Label labelToJump = mapLabelToJump.get(j);
            Label labelToVisit = mapLabelToVisit.get(j);

            if (labelToVisit != null) {
                mv.visitLabel(labelToVisit);
                mv.visitFrame(F_SAME, 0, null, 0, null);
            }


            mv.visitLdcInsn(method.hashCode()); // create an int const, used in the next if
            mv.visitVarInsn(ILOAD, 1); //Load first argument : the int hashcode methode name
            mv.visitJumpInsn(IF_ICMPNE, labelToJump); // jump to next method label

            generateDelegator(mv, className, specToDelegate, method); // generate the delegator part


        }


        mv.visitLabel(mapLabelToJump.get(numberOfMethod - 1));
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        return;
    }

    private static void generateDelegator(MethodVisitor mv, String generatedClassName, Class specToDelegate, Method method) {
        org.objectweb.asm.commons.Method asmMethod = org.objectweb.asm.commons.Method.getMethod(method);
        String interfaceName = Type.getInternalName(specToDelegate);
        mv.visitVarInsn(ALOAD, 0);

        mv.visitFieldInsn(GETFIELD, generatedClassName, POJO, POJO_TYPE);
        mv.visitTypeInsn(CHECKCAST, interfaceName);

        Class[] parametersClass = method.getParameterTypes();
        int i = 0;

        for (Class paramClass : parametersClass) {
            mv.visitVarInsn(ALOAD, 2);
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


    private static void checkCast(MethodVisitor mv, Class paramClass) {
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

    private static void boxReturn(MethodVisitor mv, Class paramClass) {
        Type paramType = Type.getType(paramClass);
        if (Type.INT_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, INTEGER, VALUE_OF_METHOD, "(I)Ljava/lang/Integer;", false);
        } else if (Type.BOOLEAN_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, BOOLEAN, VALUE_OF_METHOD, "(Z)Ljava/lang/Boolean;", false);
        } else if (Type.BYTE_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, BYTE, VALUE_OF_METHOD, "(B)Ljava/lang/Byte;", false);
        } else if (Type.CHAR_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, CHARACTER, VALUE_OF_METHOD, "(C)Ljava/lang/Character;", false);
        } else if (Type.DOUBLE_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, DOUBLE, VALUE_OF_METHOD, "(D)Ljava/lang/Double;", false);
        } else if (Type.FLOAT_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, FLOAT, VALUE_OF_METHOD, "(F)Ljava/lang/Float;", false);
        } else if (Type.LONG_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, LONG, VALUE_OF_METHOD, "(J)Ljava/lang/Long;", false);
        } else if (Type.SHORT_TYPE.equals(paramType)) {
            mv.visitMethodInsn(INVOKESTATIC, SHORT, VALUE_OF_METHOD, "(S)Ljava/lang/Short;", false);
        }
    }


}


/**
 * Ce qu'il faut savoir :
 * Liste des methodes, leurs noms
 * nbre d'attribut
 * retour
 **/