package fr.liglab.adele.cream.utils;

import org.objectweb.asm.*;

import java.lang.reflect.Method;

/**
 * Created by aygalinc on 09/09/16.
 */
public class CreamProxyGenerator implements Opcodes {

    private static final String POJO = "myPojo";

    private static final String POJO_TYPE = "L"+Type.getInternalName(Object.class)+";";

    public static byte[] dump(Class spec,Class pojoClass,String uniqueId){
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS); // use compute max to automaticali compute the max stack size and the max of local variables of a method

        String specName = Type.getInternalName(GeneratedDelegatorProxy.class);


        String[] itfs = new String[] {specName};
        String parent = "java/lang/Object";

        String className = Type.getInternalName(pojoClass) + "$$Proxy"+uniqueId.hashCode(); // Unique name.
        if (className.startsWith("java/")) {
            className = "$" + className;
        }

        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, className, null, parent, itfs);

        addPojoField(classWriter);
        generateConstructor(classWriter);
        addGetterMethod(classWriter,className);
        addSetterMethod(classWriter,className);
        addCallerMethod(classWriter,className,spec);

        classWriter.visitEnd();

        return classWriter.toByteArray();

    }

    private static void addPojoField(ClassWriter cw) {
        FieldVisitor fieldVisitor = cw.visitField(Opcodes.ACC_PRIVATE, POJO, POJO_TYPE, null, null);
        fieldVisitor.visitEnd();
    }

    private static void generateConstructor(ClassWriter cw) {
        MethodVisitor mv  =  cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);

        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);

        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V",false);

        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0);

        mv.visitEnd();

    }

    private static void addSetterMethod(ClassWriter cw,String className){
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "setPojo", "("+POJO_TYPE+")V", null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0); //Load this

        mv.visitVarInsn(ALOAD, 1); // Load the argument

        mv.visitFieldInsn(PUTFIELD, className, POJO, POJO_TYPE); // put argument on pojo field

        mv.visitInsn(RETURN); // return void

        mv.visitMaxs(0, 0);

        mv.visitEnd();


    }

    private static void addGetterMethod(ClassWriter cw,String className){
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getPojo", "()"+POJO_TYPE, null, null);

        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);

        mv.visitFieldInsn(GETFIELD, className, POJO, POJO_TYPE);

        mv.visitInsn(ARETURN);// return pojo

        mv.visitMaxs(0, 0);

        mv.visitEnd();
    }

    private static void addCallerMethod(ClassWriter cw,String className,Class specToDelegate){

        String[] exceptions = new String[] {"Ljava/lang/Throwable"};
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "delegate", "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", null,exceptions);

        String specOfDelegatedClassName = Type.getInternalName(specToDelegate);
        Method[] methods = specToDelegate.getMethods();


        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);

        mv.visitFieldInsn(GETFIELD, className, POJO, POJO_TYPE);

        mv.visitInsn(ARETURN);// return pojo

        mv.visitMaxs(0, 0);

        mv.visitEnd();
    }

}

/**
 * Ce qu'il faut savoir :
 *  Liste des methodes, leurs noms
 *  nbre d'attribut
 *  retour
 **/