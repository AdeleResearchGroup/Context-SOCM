package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public abstract class StateProcessor<A extends Annotation> extends AnnotationProcessor<A> {

	private static final  List<String> SYNCHRONISATION_ATTRIBUTES = Arrays.asList(new String[] {"push","pull","apply"});

	/**
	 * The annotation currently processed
	 */
	private A annotation;

	protected StateProcessor(Class<A> annotationType, ClassLoader classReferenceLoader) {
		super(annotationType, classReferenceLoader);
	}

	@Override
	public final void process(A annotation) {
		this.annotation = annotation;
		processStateAttributes();
	}

	protected A getAnnotation() {
		return annotation;
	}

	/**
	 * Process the state attributes specified in the annotation
	 */
	protected abstract void processStateAttributes();

	/**
	 * The id of the state referenced by the annotation
	 */
	protected abstract String getStateId();

	/**
	 * Get the global element containing all the state information
	 */
	protected Element getStateElement() {

		String stateId = getStateId();
		Element stateElement = getMetadataElement(stateId);

		/*
		 * Create the element if not already registered by another processor
		 */
		if (stateElement == null) {
			stateElement = new Element("state", "");
			stateElement.addAttribute(new Attribute("id",stateId));

			addMetadataElement(stateId,stateElement,getParentElement());
		}

		return stateElement;
	}

	/**
	 * Test whether direct access has been specified for the state element
	 */
	protected final boolean hasDirectAccess() {
		return  Boolean.valueOf(getStateElement().getAttribute("directAccess"));
	}

	/**
	 * Adds the direct access attribute for the state
	 */
	protected void setDirectAccess(boolean directAccess) {
		addStateAttribute("directAccess", Boolean.toString(directAccess), true);

		/*
         * Check if state variable have synchro function and log a warning in this case if direct access is true
         */
		if (directAccess && hasSynchronisationAttributes()){
			warn(" State Element " + getStateId() + " is in direct access but own synchro function (PUSH, PULL or APPLY). At runtime this function will not be used by the framework and affects the state.");
		}

	}

	private boolean hasSynchronisationAttributes() {
		return SYNCHRONISATION_ATTRIBUTES.stream().anyMatch(attribute -> getStateElement().getAttribute(attribute) != null);
	}

	/**
	 * Adds an attribute of the state element
	 */
	protected void addStateAttribute(String attribute, String value, boolean allowDirectAccess) {

		String stateId 			= getStateId();
		Element stateElement 	= getStateElement();
		String className 		= getAnnotatedClassType().getClassName();
		String annotationName 	= getAnnotationType().getSimpleName();

		/*
		 * Add attributes to the state element, if not already present
		 */

		if (stateElement.getAttribute(attribute) != null) {
			error("Error on class " + className + " : annotation "+ annotationName + " for state" + stateId	+ " is defined more than once");
		}

		stateElement.addAttribute(new Attribute(attribute, value));

		if (!allowDirectAccess && hasDirectAccess()) {
			warn("Warning on class " + className + " : annotation "+ annotationName + " for state" + stateId	+ ", the state has an associated field with direct access, the annotation will be ignored at runtime");
		}

	}

	private String getParentElement(){
		if ( (getRootMetadata().getNameSpace() == null ) && BehaviorProviderProcessor.COMPONENT_TYPE.equals(getRootMetadata().getName())){
			return HandlerReference.NAMESPACE+":"+HandlerReference.BEHAVIOR_ENTITY_HANDLER;
		}
		else {
			return ContextEntityProcessor.CONTEXT_ENTITY_ELEMENT;
		}
	}

}
