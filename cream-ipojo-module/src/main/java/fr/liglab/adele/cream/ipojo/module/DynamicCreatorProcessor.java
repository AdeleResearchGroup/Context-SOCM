package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import java.util.Arrays;
import java.util.List;

public class DynamicCreatorProcessor extends CreatorProcessor<Creator.Dynamic> {

    protected DynamicCreatorProcessor(ClassLoader classReferenceLoader) {
        super(Creator.Dynamic.class, classReferenceLoader);
    }

    @Override
    protected void processCreator(Element creator) {

    	List<String> typeArguments = new TypeArgumentExtractor(getAnnotatedField().signature).getTypeArguments();
        if ( typeArguments.size() == 2 && typeArguments.get(0).equals("org.apache.felix.ipojo.Factory") && typeArguments.get(1).equals("fr.liglab.adele.cream.annotations.provider.Creator$Entity")) {
            creator.addAttribute(new Attribute("dynamic", "true"));

            OriginEnum origin		= getAnnotation().origin();
            Class[] requirements	= getAnnotation().requirements();
            creator.addAttribute(new Attribute("origin", origin.getValue()));
            creator.addAttribute(new Attribute("requirements", Arrays.toString(requirements)));
        } else {
            error("Dynamic creator field '%s' in class %s must have type Function<Factory,Creator.Entity>",
                    getAnnotatedField().name, getAnnotatedClass().name);
        }
    }

}
