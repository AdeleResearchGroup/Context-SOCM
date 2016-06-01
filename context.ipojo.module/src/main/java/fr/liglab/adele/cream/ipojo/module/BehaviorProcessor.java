package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

/**
 * Created by aygalinc on 15/01/16.
 */
public class BehaviorProcessor extends AnnotationProcessor<Behavior>  {

	protected static final String BEHAVIOR_ELEMENT = BehaviorReference.NAMESPACE+":"+BehaviorReference.DEFAULT_BEHAVIOR_NAME;

	public BehaviorProcessor(ClassLoader classReferenceLoader) {
		super(Behavior.class,classReferenceLoader);
	}

	@Override
	public void process(Behavior annotation) {
		Element root = getRootMetadata();
		root.addElement(buildBehaviorElement(annotation));
	}

	private Element buildBehaviorElement(Behavior annotation){
		Element behaviorElement = new Element(BehaviorReference.DEFAULT_BEHAVIOR_NAME,BehaviorReference.NAMESPACE);
		Attribute specAttr = new Attribute(BehaviorReference.SPEC_ATTR_NAME,annotation.spec().getName());
		Attribute implAttr = new Attribute(BehaviorReference.IMPLEM_ATTR_NAME,annotation.implem().getName());
		behaviorElement.addAttribute(specAttr);
		behaviorElement.addAttribute(implAttr);
		return behaviorElement;
	}
}
