package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.behavior.InjectedBehavior;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.tree.FieldNode;

public class InjectedBehaviorProcessor extends AnnotationProcessor<InjectedBehavior> {


	public InjectedBehaviorProcessor(ClassLoader classReferenceLoader) {
		super(InjectedBehavior.class,classReferenceLoader);
	}

	@Override
	public final void process(InjectedBehavior annotation) {


		FieldNode node = getAnnotatedField();
		Element element = getMetadataElement(BehaviorReference.BEHAVIOR_INDIVIDUAL_ELEMENT_NAME+":"+annotation.id());
		if (element == null){
			error("Wrong injected behavior id. No declared behavior corresponds to id : " + node.name);
		}
		checkFieldTypeCorrespondance(element,node.desc);

		Attribute attributeField = new Attribute(BehaviorReference.FIELD_ATTRIBUTE_NAME,node.name);
		element.addAttribute(attributeField);


		return;
	}

	private void checkFieldTypeCorrespondance(Element behaviorElement,String desc){
		String javaType = convertASMType(desc);
		if (!(behaviorElement.getAttribute(BehaviorReference.SPECIFICATION_ATTRIBUTE_NAME).equals(javaType))){
			error("Behavior injected field with id " + behaviorElement.getAttribute(BehaviorReference.ID_ATTRIBUTE_NAME) + " have a specification ( "+ javaType+" ) that not match the corresponding behavior annotation ( "+ behaviorElement.getAttribute(BehaviorReference.SPECIFICATION_ATTRIBUTE_NAME)+" ).");
		}
	}

	private String convertASMType(String asmType){
		String subAsm = asmType.substring(1,asmType.length()-1);
		String javaType = subAsm.replaceAll("/",".");
		return javaType;
	}
}
