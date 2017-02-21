package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.entity.ContextEntity.Relation;
import fr.liglab.adele.cream.annotations.entity.ContextEntity.State;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtensions;
import fr.liglab.adele.cream.annotations.functional.extension.InjectedFunctionalExtension;
import fr.liglab.adele.cream.annotations.provider.Creator;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.manipulator.spi.AbsBindingModule;
import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.apache.felix.ipojo.manipulator.spi.Predicate;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.List;

import static org.apache.felix.ipojo.manipulator.spi.helper.Predicates.*;

/**
 * Created by aygalinc on 14/01/16.
 */
public class ContextBindingModule extends AbsBindingModule {
    private static final AnnotationVisitor error(BindingContext context, String message, Object... args) {
        context.getReporter().error(message, args);
        return null;
    }

    private static final Predicate not(Predicate predicate) {
        return context -> !predicate.matches(context);
    }

    public static Method method() {
        return new Method();
    }

    public static Field field() {
        return new Field();
    }

    /**
     * Adds the Wisdom annotation to the iPOJO manipulator.
     */
    @Override
    public void configure() {

    	/*
    	 * The loader used to load the classes referenced in annotations. Notice that we try to load classes using
    	 * the class loader of this module, and if it is not possible we use a loader that creates an empty mocked-up
    	 * class that represent the referenced class.
    	 */
        ClassLoader classReferenceLoader = new AnnotationBuilder.ClassReferenceLoader(this.getClass().getClassLoader());

    	/*
    	 * Bind the context entity annotation processors
    	 */
        bind(ContextEntity.class)
                .to(
                        new ContextEntityProcessor(classReferenceLoader)
                );

        bind(FunctionalExtender.class)
                .to(
                        new FunctionalExtenderProcessor(classReferenceLoader)
                );
        bind(FunctionalExtender.ChangeOn.class)
                .when(and(on(ElementType.METHOD), reference(FunctionalExtenderProcessor.FUNCTIONAL_EXTENSION_CONTEXT_ENTITY_ELEMENT).exists()))
                .to(
                        new FunctionalExtenderChangeOnProcessor(classReferenceLoader)
                );
        bind(FunctionalExtension.class)
                .when(and(on(ElementType.TYPE), reference(ContextEntityProcessor.CONTEXT_ENTITY_ELEMENT).exists()))
                .to(
                        new FunctionalExtensionProcessor(classReferenceLoader)
                );

        bind(FunctionalExtensions.class)
                .when(and(on(ElementType.TYPE), reference(ContextEntityProcessor.CONTEXT_ENTITY_ELEMENT).exists()))
                .to(
                        new FunctionalExtensionsProcessor(classReferenceLoader)
                );

        bind(InjectedFunctionalExtension.class)
                .when(and(on(ElementType.FIELD), reference(ContextEntityProcessor.CONTEXT_ENTITY_ELEMENT).exists(), reference(AbstractFunctionalExtensionProcessor.BEHAVIOR_ELEMENT).exists()))
                .to(
                        new InjectedBehaviorProcessor(classReferenceLoader)
                );

        bind(State.Field.class)
                .when(getFieldSuccessPredicate())
                .to(
                        new StateVariableFieldProcessor(classReferenceLoader)
                );

        bind(State.Pull.class)
                .when(getFieldSuccessPredicate())
                .to(
                        new PullFieldProcessor(classReferenceLoader)
                );

        bind(State.Apply.class)
                .when(getFieldSuccessPredicate())
                .to(
                        new ApplyFieldProcessor(classReferenceLoader)
                );

        bind(State.Push.class)
                .when(or(and(on(ElementType.METHOD), not(method().returns(Void.TYPE)), reference(ContextEntityProcessor.CONTEXT_ENTITY_ELEMENT).exists()),
                        and(on(ElementType.METHOD), not(method().returns(Void.TYPE)), reference(FunctionalExtenderProcessor.FUNCTIONAL_EXTENSION_CONTEXT_ENTITY_ELEMENT).exists())))
                .to(
                        new PushMethodProcessor(classReferenceLoader)
                );
        bind(Relation.Field.class)
                .when(getFieldSuccessPredicate())
                .to(
                        new RelationProcessor(classReferenceLoader)
                );

    	/*
    	 * Bind the context provider annotation processors
    	 */
        bind(Creator.Field.class)
                .when(and(on(ElementType.FIELD), field().hasType(Creator.Entity.class)))
                .to(
                        new EntityProviderProcessor(classReferenceLoader)
                );

        bind(Creator.Field.class)
                .when(and(on(ElementType.FIELD), field().hasType(Creator.Relation.class)))
                .to(
                        new RelationProviderProcessor(classReferenceLoader)
                );


    	/*
    	 * Bind empty processors to error conditions
    	 */

        bind(State.Field.class)
                .when(getFieldFailPredicate())
                .to((BindingContext context) ->
                        error(context, "Class %s must be annotated with %s or %s to use State injection annotation",
                                context.getWorkbench().getClassNode().name, ContextEntity.class.getSimpleName(), FunctionalExtender.class.getSimpleName())
                );

        bind(State.Pull.class)
                .when(getFieldFailPredicate())
                .to((BindingContext context) ->
                        error(context, "Class %s must be annotated with %s or %s to use pull annotation",
                                context.getWorkbench().getClassNode().name, ContextEntity.class.getSimpleName(), FunctionalExtender.class.getSimpleName())
                );


        bind(State.Apply.class)
                .when(getFieldFailPredicate())
                .to((BindingContext context) ->
                        error(context, "Class %s must be annotated with %s or %s to use aply annotation",
                                context.getWorkbench().getClassNode().name, ContextEntity.class.getSimpleName(), FunctionalExtender.class.getSimpleName())
                );

        bind(State.Push.class)
                .when(and(on(ElementType.METHOD), not(or(reference(ContextEntityProcessor.CONTEXT_ENTITY_ELEMENT).exists(), reference(FunctionalExtenderProcessor.FUNCTIONAL_EXTENSION_CONTEXT_ENTITY_ELEMENT).exists()))))
                .to((BindingContext context) ->
                        error(context, "Class %s must be annotated with %s or %s to use push injection annotation",
                                context.getWorkbench().getClassNode().name, ContextEntity.class.getSimpleName(), FunctionalExtender.class.getSimpleName())
                );

        bind(State.Push.class)
                .when(and(on(ElementType.METHOD), method().returns(Void.TYPE)))
                .to((BindingContext context) ->
                        error(context, "Push method '%s' in class %s must have a return type. The value of this return is affected in the state buffer each time the method is called.",
                                context.getMethodNode().name, context.getWorkbench().getClassNode().name)
                );

        bind(FunctionalExtension.class)
                .when(and(on(ElementType.TYPE), not(reference(ContextEntityProcessor.CONTEXT_ENTITY_ELEMENT).exists())))
                .to((BindingContext context) ->
                        error(context, "Annotation '%s' in class %s must be used only on class annotated with %s",
                                FunctionalExtension.class.getSimpleName(), context.getWorkbench().getClassNode().name, ContextEntity.class.getSimpleName())
                );

        bind(FunctionalExtensions.class)
                .when(and(on(ElementType.TYPE), not(reference(ContextEntityProcessor.CONTEXT_ENTITY_ELEMENT).exists())))
                .to((BindingContext context) ->
                        error(context, "Annotation '%s' in class %s must be used only on class annotated with %s",
                                FunctionalExtension.class.getSimpleName(), context.getWorkbench().getClassNode().name, ContextEntity.class.getSimpleName())
                );
        bind(FunctionalExtender.ChangeOn.class)
                .when(and(on(ElementType.TYPE), not(reference(FunctionalExtenderProcessor.FUNCTIONAL_EXTENSION_CONTEXT_ENTITY_ELEMENT).exists())))
                .to((BindingContext context) ->
                        error(context, "Annotation '%s' in class %s must be used only on class annotated with %s",
                                FunctionalExtender.ChangeOn.class.getSimpleName(), context.getWorkbench().getClassNode().name, FunctionalExtender.class.getSimpleName())
                );
        bind(InjectedFunctionalExtension.class)
                .when(and(on(ElementType.FIELD), not(or(reference(ContextEntityProcessor.CONTEXT_ENTITY_ELEMENT).exists(), reference(AbstractFunctionalExtensionProcessor.BEHAVIOR_ELEMENT).exists()))))
                .to((BindingContext context) ->
                        error(context, "Class %s must be annotated with %s and %s to use injected behavior annotation",
                                context.getWorkbench().getClassNode().name, ContextEntity.class.getSimpleName(), FunctionalExtension.class.getSimpleName())
                );

        /*
         * TODO currently the iPOJO annotation matadata provider doesn't give access to all the annotations of
         * the field
         */
        bind(Relation.Field.class)
                .when(and(on(ElementType.FIELD), not(field().hasAnnotation(Requires.class))))
                .to(
                        new RelationProcessor(classReferenceLoader)
        		/*
        		(BindingContext context) ->
    			error(context,"Relation field '%s' in class %s must be annotated using iPOJO annotation 'Requires'",
    					context.getFieldNode().name, context.getWorkbench().getClassNode().name)
    			*/
                );


        bind(Creator.Field.class)
                .when(and(on(ElementType.FIELD),
                        not(or(field().hasType(Creator.Entity.class), field().hasType(Creator.Relation.class)))
                ))
                .to((BindingContext context) ->
                        error(context, "Creator field '%s' in class %s must have type Creator.Entity or Creator.Relation",
                                context.getFieldNode().name, context.getWorkbench().getClassNode().name)
                );

    }

    public Predicate getFieldSuccessPredicate() {
        return or(and(on(ElementType.FIELD), reference(ContextEntityProcessor.CONTEXT_ENTITY_ELEMENT).exists()), and(on(ElementType.FIELD), reference(FunctionalExtenderProcessor.FUNCTIONAL_EXTENSION_CONTEXT_ENTITY_ELEMENT).exists()));
    }

    public Predicate getFieldFailPredicate() {
        return and(on(ElementType.FIELD), not(or(reference(ContextEntityProcessor.CONTEXT_ENTITY_ELEMENT).exists(), reference(FunctionalExtenderProcessor.FUNCTIONAL_EXTENSION_CONTEXT_ENTITY_ELEMENT).exists())));
    }

    public static class Method {
        /**
         * Restrict execution if the supported {@literal MethidNode} has the given return type.
         */
        public Predicate returns(final Class<?> expected) {
            return context -> context.getMethodNode() != null &&
                    Type.getReturnType(context.getMethodNode().desc).equals(Type.getType(expected));

        }
    }

    public static class Field {

        /**
         * Checks if a field node's declared annotations node contain the expected annotation
         */
        @SuppressWarnings("unchecked")
        private static <A extends Annotation> boolean hasAnnotation(FieldNode field, Class<A> expected) {
            return hasAnnotation((List<AnnotationNode>) field.invisibleAnnotations, expected) ||
                    hasAnnotation((List<AnnotationNode>) field.visibleAnnotations, expected);
        }

        /**
         * Checks if a list of annotations node contains the expected annotation
         */
        private static <A extends Annotation> boolean hasAnnotation(List<AnnotationNode> annotations, Class<A> expected) {

            if (annotations == null)
                return false;

            for (AnnotationNode annotation : annotations) {
                if (Type.getType(annotation.desc).equals(Type.getType(expected))) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Restrict execution if the supported {@literal FieldNode} has the given type.
         */
        public Predicate hasType(final Class<?> expected) {
            return context -> context.getFieldNode() != null &&
                    Type.getType(context.getFieldNode().desc).equals(Type.getType(expected));

        }

        /**
         * Restrict execution if the supported {@literal FieldNode} is annotated with the given type.
         */
        public <A extends Annotation> Predicate hasAnnotation(final Class<A> expected) {
            return context -> context.getFieldNode() != null &&
                    hasAnnotation(context.getFieldNode(), expected);

        }
    }
}

