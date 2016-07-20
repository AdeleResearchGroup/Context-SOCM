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
		Element root = getRootMetadata();
		String classname 			= getAnnotatedClassType().getClassName();

		if (!checkRootElement(root)){
			error("Behavior annotation find on a different component type than " + ContextEntityProcessor.COMPONENT_TYPE, classname);
		}

		root.addElement(buildBehaviorElement(annotation));
	}

	private Element buildBehaviorElement(Behavior annotation){
		Element behaviorElement = new Element(HandlerReference.BEHAVIOR_MANAGER_HANDLER,HandlerReference.NAMESPACE);
		Attribute specAttr = new Attribute(BehaviorReference.SPECIFICATION_ATTRIBUTE_NAME,annotation.spec().getName());
		Attribute implAttr = new Attribute(BehaviorReference.IMPLEMEMENTATION_ATTRIBUTE_NAME,annotation.implem().getName());
		Attribute id = new Attribute(BehaviorReference.ID_ATTRIBUTE_NAME,annotation.id());
		behaviorElement.addAttribute(specAttr);
		behaviorElement.addAttribute(implAttr);
		behaviorElement.addAttribute(id);
		return behaviorElement;
	}

	private boolean checkRootElement(Element root){
		if (ContextEntityProcessor.COMPONENT_TYPE.equals(root.getName())){
			return true;
		}
		return false;
	}
}
