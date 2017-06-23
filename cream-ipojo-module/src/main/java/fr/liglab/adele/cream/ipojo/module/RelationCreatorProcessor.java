package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.entity.ContextEntity.Relation;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import java.util.Arrays;
import java.util.List;

public class RelationCreatorProcessor extends CreatorProcessor<Creator.Field> {

    protected RelationCreatorProcessor(ClassLoader classReferenceLoader) {
        super(Creator.Field.class, classReferenceLoader);
    }

    @Override
    protected void processCreator(Element creator) {

        List<String> typeArguments = new TypeArgumentExtractor(getAnnotatedField().signature).getTypeArguments();

        String sourceEntity = typeArguments.size() == 2 ? typeArguments.get(0) : null;
        String targetEntity = typeArguments.size() == 2 ? typeArguments.get(1) : null;
        String relation = getAnnotation().value();
        OriginEnum origin		= getAnnotation().origin();
        Class[] requirements	= getAnnotation().requirements();

        if (sourceEntity != null && targetEntity != null && !relation.equals(Creator.Field.NO_PARAMETER)) {
            creator.addAttribute(new Attribute("entity", sourceEntity));
            creator.addAttribute(new Attribute("relation", Relation.id(getSimpleClassName(sourceEntity), relation)));
            creator.addAttribute(new Attribute("target", targetEntity));
            creator.addAttribute(new Attribute("origin", origin.getValue()));
            creator.addAttribute(new Attribute("requirements", Arrays.toString(requirements)));
        } else if (sourceEntity == null || targetEntity == null) {
            error("relation creator field '%s' in class %s must parameterize type Creator.relation with the source and target entity class",
                    getAnnotatedField().name, getAnnotatedClass().name);
        } else if (relation.equals(Creator.Field.NO_PARAMETER)) {
            error("relation creator field '%s' in class %s must specify the name of the relation",
                    getAnnotatedField().name, getAnnotatedClass().name);
        }
    }

}