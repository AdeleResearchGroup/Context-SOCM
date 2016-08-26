package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import java.lang.annotation.Annotation;

public abstract class ProviderProcessor<A extends Annotation> extends AnnotationProcessor<A> {

	/**
	 * The annotation currently processed
	 */
	private A annotation;

	protected ProviderProcessor(Class<A> annotationType, ClassLoader classReferenceLoader) {
		super(annotationType, classReferenceLoader);
	}

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
