package fr.liglab.adele.cream.test.proxy;

import java.util.*;
        import org.objectweb.asm.*;
public class ExampleDump implements Opcodes {

    public static byte[] dump () throws Exception {

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, "fr/liglab/adele/cream/utils/Example", null, "java/lang/Object", new String[] { "fr/liglab/adele/cream/utils/GeneratedDelegatorProxy" });

        {
            fv = cw.visitField(ACC_PRIVATE, "myPojo", "Ljava/lang/Object;", null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "setPojo", "(Ljava/lang/Object;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, "fr/liglab/adele/cream/utils/Example", "myPojo", "Ljava/lang/Object;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getPojo", "()Ljava/lang/Object;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "fr/liglab/adele/cream/utils/Example", "myPojo", "Ljava/lang/Object;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "delegate", "(I[Ljava/lang/Object;)Ljava/lang/Object;", null, new String[] { "java/lang/Throwable" });
            mv.visitCode();
            mv.visitVarInsn(ILOAD, 1);
            mv.visitIntInsn(SIPUSH, 4533);

            Label l0 = new Label();
            mv.visitJumpInsn(IF_ICMPNE, l0);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "fr/liglab/adele/cream/utils/Example", "myPojo", "Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "org/apache/felix/ipojo/Pojo");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/apache/felix/ipojo/Pojo", "getComponentInstance", "()Lorg/apache/felix/ipojo/ComponentInstance;", true);
            mv.visitInsn(ARETURN);

            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitLdcInsn(new Integer(4533545));

            Label l1 = new Label();
            mv.visitJumpInsn(IF_ICMPNE, l1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "fr/liglab/adele/cream/utils/Example", "myPojo", "Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "org/apache/felix/ipojo/Pojo");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/apache/felix/ipojo/Pojo", "getComponentInstance", "()Lorg/apache/felix/ipojo/ComponentInstance;", true);
            mv.visitInsn(ARETURN);

            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitLdcInsn(new Integer(4533546));

            Label l2 = new Label();
            mv.visitJumpInsn(IF_ICMPNE, l2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "fr/liglab/adele/cream/utils/Example", "myPojo", "Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "wait", "()V", false);
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);

            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitLdcInsn(new Integer(4533546));

            Label l3 = new Label();
            mv.visitJumpInsn(IF_ICMPNE, l3);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "fr/liglab/adele/cream/utils/Example", "myPojo", "Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(AALOAD);
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
            mv.visitInsn(ARETURN);

            mv.visitLabel(l3);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        cw.visitEnd();

        System.out.print(cw.toByteArray().toString());
        return cw.toByteArray();
    }
}
