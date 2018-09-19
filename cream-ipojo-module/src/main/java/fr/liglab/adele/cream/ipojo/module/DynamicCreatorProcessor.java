package fr.liglab.adele.cream.ipojo.module;

import java.util.List;

import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import fr.liglab.adele.cream.annotations.provider.Creator;


public class DynamicCreatorProcessor extends CreatorProcessor<Creator.Dynamic> {

    protected DynamicCreatorProcessor(ClassLoader classReferenceLoader) {
        super(Creator.Dynamic.class, classReferenceLoader);
    }

    @Override
    protected void processCreator(Element creator) {

    	List<String> typeArguments = new TypeArgumentExtractor(getAnnotatedField().signature).getTypeArguments();
        if ( typeArguments.size() == 2 && typeArguments.get(0).equals("org.apache.felix.ipojo.Factory") && typeArguments.get(1).equals("fr.liglab.adele.cream.annotations.provider.Creator$Entity")) {

        	creator.addAttribute(new Attribute("dynamic", "true"));

        } else {
            error("Dynamic creator field '%s' in class %s must have type Function<Factory,Creator.Entity>",
                    getAnnotatedField().name, getAnnotatedClass().name);
        }
    }

}
