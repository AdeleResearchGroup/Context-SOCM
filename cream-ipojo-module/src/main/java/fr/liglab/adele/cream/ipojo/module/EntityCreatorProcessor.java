package fr.liglab.adele.cream.ipojo.module;

import java.util.List;

import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import fr.liglab.adele.cream.annotations.provider.Creator;

public class EntityCreatorProcessor extends CreatorProcessor<Creator.Field> {

    protected EntityCreatorProcessor(ClassLoader classReferenceLoader) {
        super(Creator.Field.class, classReferenceLoader);
    }

    @Override
    protected void processCreator(Element creator) {
    	
        List<String> typeArguments	= new TypeArgumentExtractor(getAnnotatedField().signature).getTypeArguments();
        String entityType       	= typeArguments.size() == 1 ? typeArguments.get(0) : null;
        

        if (entityType != null) {
            creator.addAttribute(new Attribute("entity", entityType));
        } else {
            error("Entity creator field '%s' in class %s must parameterize type Creator.Entity with the entity class",
                    getAnnotatedField().name, getAnnotatedClass().name);
        }
    }

}