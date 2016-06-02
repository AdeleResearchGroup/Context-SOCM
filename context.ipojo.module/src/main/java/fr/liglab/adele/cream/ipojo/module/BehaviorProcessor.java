package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

/**
 * Created by aygalinc on 15/01/16.
 */
public class BehaviorProcessor extends AnnotationProcessor<Behavior>  {

	protected static final String BEHAVIOR_ELEMENT = BehaviorReference.BEHAVIOR_NAMESPACE +":"+BehaviorReference.DEFAULT_BEHAVIOR_TYPE;

	public BehaviorProcessor(ClassLoader classReferenceLoader) {
		super(Behavior.class,classReferenceLoader);
	}

	@Override
	public void process(Behavior annotation) {
		Element root = getRootMetadata();
		root.addElement(buildBehaviorElement(annotation));
	}

	private Element buildBehaviorElement(Behavior annotation){
		Element behaviorElement = new Element(BehaviorReference.DEFAULT_BEHAVIOR_TYPE,BehaviorReference.BEHAVIOR_NAMESPACE);
		Attribute specAttr = new Attribute(BehaviorReference.SPEC_ATTR_NAME,annotation.spec().getName());
		Attribute implAttr = new Attribute(BehaviorReference.IMPLEM_ATTR_NAME,annotation.implem().getName());
		Attribute id = new Attribute(BehaviorReference.ID_ATTR_NAME,annotation.id());
		behaviorElement.addAttribute(specAttr);
		behaviorElement.addAttribute(implAttr);
		behaviorElement.addAttribute(id);
		return behaviorElement;
	}
}
