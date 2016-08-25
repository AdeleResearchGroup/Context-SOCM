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

		Attribute attributeField = new Attribute(BehaviorReference.FIELD_ATTRIBUTE_NAME,node.name);
		element.addAttribute(attributeField);


		return;
	}


	private void printElement(Element element2 ){
		if (element2 == null){
			System.out.println("Element is null");
			return;
		}
		System.out.println("Element ns:" + element2.getNameSpace() + " , n: " + element2.getName());
		Attribute[] attr = element2.getAttributes();
		if (attr != null){
			for (Attribute attribute:attr){
				System.out.println("Attr ns: " + attribute.getName()+ " , n: " + attribute.getName() +" , value: " + attribute.getValue());
			}
		}

		Element[] elements = element2.getElements();

		if (elements != null){
			System.out.println("SubElements");
			for (Element element : elements){
				System.out.println("Recursion ");
				printElement(element);
			}
		}else {
			System.out.println("No subElements");
		}

	}

}
