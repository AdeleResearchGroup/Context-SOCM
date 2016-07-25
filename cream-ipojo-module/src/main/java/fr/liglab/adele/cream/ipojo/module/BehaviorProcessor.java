package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

/**
 * Created by aygalinc on 15/01/16.
 */
public class BehaviorProcessor extends AnnotationProcessor<Behavior>  {

	protected static final String BEHAVIOR_ELEMENT = HandlerReference.NAMESPACE +":"+ HandlerReference.BEHAVIOR_MANAGER_HANDLER;

	public BehaviorProcessor(ClassLoader classReferenceLoader) {
		super(Behavior.class,classReferenceLoader);
	}

	@Override
	public void process(Behavior annotation) {
		buildSubBehaviorElement(annotation);
	}

	private void buildSubBehaviorElement(Behavior annotation){
		Element behaviorIndividualElement = new Element(BehaviorReference.BEHAVIOR_INDIVIDUAL_ELEMENT_NAME,"");
		Attribute specAttr = new Attribute(BehaviorReference.SPECIFICATION_ATTRIBUTE_NAME,annotation.spec().getName());
		Attribute implAttr = new Attribute(BehaviorReference.IMPLEMEMENTATION_ATTRIBUTE_NAME,annotation.implem().getName());
		Attribute id = new Attribute(BehaviorReference.ID_ATTRIBUTE_NAME,annotation.id());
		behaviorIndividualElement.addAttribute(specAttr);
		behaviorIndividualElement.addAttribute(implAttr);
		behaviorIndividualElement.addAttribute(id);

		addMetadataElement(BehaviorReference.BEHAVIOR_INDIVIDUAL_ELEMENT_NAME,behaviorIndividualElement,getBehaviorParentElement());

	}

	private boolean checkRootElement(Element root){
		if (ContextEntityProcessor.COMPONENT_TYPE.equals(root.getName())){
			return true;
		}
		return false;
	}

	private String getBehaviorParentElement(){
		Element behaviorElement = getMetadataElement(BEHAVIOR_ELEMENT);

		if (behaviorElement == null){
			behaviorElement = new Element(HandlerReference.BEHAVIOR_MANAGER_HANDLER,HandlerReference.NAMESPACE);
			addMetadataElement(BEHAVIOR_ELEMENT,behaviorElement);
		}
		return BEHAVIOR_ELEMENT;
	}


}
