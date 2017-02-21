package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;


public class FunctionalExtenderChangeOnProcessor extends AnnotationProcessor<FunctionalExtender.ChangeOn> {


	public FunctionalExtenderChangeOnProcessor(ClassLoader classReferenceLoader) {
		super(FunctionalExtender.ChangeOn.class,classReferenceLoader);
	}

	@Override
	public final void process(FunctionalExtender.ChangeOn annotation) {

		Class spec = annotation.contextService();
		String id = annotation.id();
		MethodNode method = getAnnotatedMethod();

		Type[] types = Type.getArgumentTypes(method.desc);

		if (types == null || types.length != 1){
			error("Must annotated with ChangeOn must contains only 1 parameter , on method ",method.name );
		}

		String propertyId = ContextEntity.State.id(spec,id);

		Element behaviorLifecycleElement = getMetadataElement(FunctionalExtenderProcessor.FUNCTIONAL_EXTENSION_LIFECYCLE_ELEMENT);
		Element onChangeElement = new Element("changeOn","");

		Attribute idAttribute = new Attribute("id",propertyId);
		Attribute methodAttribute = new Attribute("method",method.name);

		onChangeElement.addAttribute(idAttribute);
		onChangeElement.addAttribute(methodAttribute);

		behaviorLifecycleElement.addElement(onChangeElement);
	}

}
