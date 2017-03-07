package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.provider.Creator;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import java.util.Arrays;
import java.util.List;

public class EntityCreatorProcessor extends CreatorProcessor<Creator.Field> {

    protected EntityCreatorProcessor(ClassLoader classReferenceLoader) {
        super(Creator.Field.class, classReferenceLoader);
    }

    @Override
    protected void processCreator(Element creator) {
        List<String> typeArguments = new TypeArgumentExtractor(getAnnotatedField().signature).getTypeArguments();
        String entityType = typeArguments.size() == 1 ? typeArguments.get(0) : null;
        Class[] requirements		= getAnnotation().requirements();

        if (entityType != null) {
            creator.addAttribute(new Attribute("entity", entityType));
            creator.addAttribute(new Attribute("requirements", Arrays.toString(requirements)));
        } else {
            error("Entity creator field '%s' in class %s must parameterize type Creator.Entity with the entity class",
                    getAnnotatedField().name, getAnnotatedClass().name);
        }
    }

}
