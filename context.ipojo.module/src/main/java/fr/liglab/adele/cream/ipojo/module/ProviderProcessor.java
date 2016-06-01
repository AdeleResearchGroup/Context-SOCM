package fr.liglab.adele.cream.ipojo.module;

import java.lang.annotation.Annotation;

import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;

public abstract class ProviderProcessor<A extends Annotation> extends AnnotationProcessor<A> {


	protected ProviderProcessor(Class<A> annotationType, ClassLoader classReferenceLoader) {
		super(annotationType, classReferenceLoader);
	}

	/**
	 * The annotation currently processed
	 */
	private A annotation;

	@Override
	public final void process(A annotation) {
		this.annotation = annotation;
		
        /*
         *  Create the creator element and initiliaze with the data in the annotation
         */
        Element creator = new Element(HandlerReference.CREATOR_HANDLER,HandlerReference.NAMESPACE);
        creator.addAttribute(new Attribute("field",getAnnotatedField().name));
		processCreator(creator);
		
		addMetadataElement(creator);
	}

	protected A getAnnotation() {
		return annotation;
	}

	/**
	 * Process the creator specified by the annotation
	 */
	protected abstract void processCreator(Element creator);

}
