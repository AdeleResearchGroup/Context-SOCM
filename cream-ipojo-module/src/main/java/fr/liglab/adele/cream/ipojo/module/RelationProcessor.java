package fr.liglab.adele.cream.ipojo.module;

import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import fr.liglab.adele.cream.annotations.entity.ContextEntity.Relation;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;

public class RelationProcessor extends AnnotationProcessor<Relation.Field> {


	public RelationProcessor(ClassLoader classReferenceLoader) {
		super(Relation.Field.class,classReferenceLoader);
	}

	@Override
	public final void process(Relation.Field annotation) {

		String owner		= Relation.Field.DEFAULT_OWNER.equals(annotation.owner()) ? getAnnotatedClassName(true) : getSimpleClassName(annotation.owner().getName());
		String relationId	= Relation.ID(owner,annotation.value());

		Element relation 	= new Element(HandlerReference.RELATION_HANDLER,HandlerReference.NAMESPACE);
		relation.addAttribute(new Attribute("relation",relationId));
		relation.addAttribute(new Attribute("field",getAnnotatedField().name));
		
		addMetadataElement(relation);
	}


}
