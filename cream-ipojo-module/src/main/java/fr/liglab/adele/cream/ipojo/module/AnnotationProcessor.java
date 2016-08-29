package fr.liglab.adele.cream.ipojo.module;

import org.apache.felix.ipojo.manipulator.metadata.annotation.ComponentWorkbench;
import org.apache.felix.ipojo.manipulator.spi.AnnotationVisitorFactory;
import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;


/**
 * A class that processes annotations on instrumented iPOJO classes
 *
 * @author vega
 *
 */
public abstract class AnnotationProcessor<A extends Annotation> implements AnnotationVisitorFactory {

	/**
	 * The context of the current processing
	 */
	private BindingContext context;

	/**
	 * The annotation type handle by this processor
	 */
	private final Class<A> annotationType;

	/**
	 * The class loader used to load all class references in annotations bound to this processor
	 */
	private final ClassLoader classReferenceLoader;

	protected AnnotationProcessor(Class<A> annotationType, ClassLoader classReferenceLoader) {
		this.annotationType 		= annotationType;
		this.classReferenceLoader	= classReferenceLoader;
	}

	@Override
	public AnnotationVisitor newAnnotationVisitor(final BindingContext context) {
		
		/*
		 * Create an annotation builder that will visit the annotation information using ASM and create the
		 * corresponding annotation instance that will be consumed by this processor
		 */
		return new AnnotationBuilder<A>(annotationType, classReferenceLoader, annotation -> this.process(context, annotation));
	}

	protected Class<A> getAnnotationType() {
		return annotationType;
	}

	protected ComponentWorkbench getWorkbench() {
		return context.getWorkbench();
	}

	protected void error(String message, Object... args) {
		context.getReporter().error(message, args);
	}

	protected void warn(String message, Object... args) {
		context.getReporter().warn(message, args);
	}

	protected ClassNode getAnnotatedClass() {
		return getWorkbench().getClassNode();
	}

	protected Type getAnnotatedClassType() {
		return getWorkbench().getType();
	}

	protected String getAnnotatedClassName() {
		return	getAnnotatedClassName(false);
	}

	protected String getAnnotatedClassName(boolean simple) {
		String className 	= getAnnotatedClassType().getClassName();
		return	simple ? getSimpleClassName(className) : className;
	}

	protected static final String getSimpleClassName(String className) {
		return className.substring(className.lastIndexOf('.')+1);
	}

	protected MethodNode getAnnotatedMethod() {
		return context.getMethodNode();
	}

	protected FieldNode getAnnotatedField() {
		return context.getFieldNode();
	}

	protected Element getRootMetadata() {
		return getWorkbench().getRoot();
	}

	protected void setRootMetadata(Element root) {
		getWorkbench().setRoot(root);
	}

	protected Element getMetadataElement(String id) {
		return getWorkbench().getIds().get(id);
	}

	protected void addMetadataElement(Element element) {
		addMetadataElement(element,null);
	}

	protected void addMetadataElement(String id, Element element) {
		addMetadataElement(id,element,null);
	}

	protected void addMetadataElement(Element element, String parent) {
		addMetadataElement(null,element,parent);
	}

	protected void addMetadataElement(String id, Element element, String parent) {
		getWorkbench().getElements().put(element,parent);
		if (id != null) {
			getWorkbench().getIds().put(id,element);
		}
	}

	/**
	 * Process the annotation instance in the specified binding context
	 */
	public void process(BindingContext context, A annotation) {
		this.context	= context;
		process(annotation);
	}


	/**
	 * Process the built annotation
	 */
	public abstract void process(A annotation);




}
