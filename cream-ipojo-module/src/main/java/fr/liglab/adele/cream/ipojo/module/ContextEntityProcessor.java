package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.entity.ContextProvideStrategy;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by aygalinc on 14/01/16.
 */
public class ContextEntityProcessor extends AnnotationProcessor<ContextEntity> {

    public static final String COMPONENT_TYPE = "context-component";
    public static final String CONTEXT_PROVIDE_TYPE = "context-provide";
    protected static final String CONTEXT_ENTITY_ELEMENT = HandlerReference.NAMESPACE + ":" + HandlerReference.ENTITY_HANDLER;

    public ContextEntityProcessor(ClassLoader classReferenceLoader) {
        super(ContextEntity.class, classReferenceLoader);
    }

    @Override
    public void process(ContextEntity annotation) {

    	/*
    	 * Create the corresponding root iPOJO component
    	 */
        Element component = new Element(COMPONENT_TYPE, "");
        String classname = getAnnotatedClassType().getClassName();

        component.addAttribute(new Attribute("classname", classname));
        component.addAttribute(new Attribute("immediate", "true"));

        if (getRootMetadata() != null) {
            error("Multiple 'component type' annotations on the class '{%s}'.", classname);
            warn("@Entity is ignored.");
            component = getRootMetadata();
        }

        setRootMetadata(component);
        
        /*
         * Verify the annotated class implements all the context coreServices specified in the annotation
         */
        ClassNode clazz = getAnnotatedClass();
        boolean implemented = true;

        for (Class<?> service : annotation.coreServices()) {

            if (!clazz.interfaces.contains(Type.getInternalName(service))) {
                error("Class " + clazz.name + " is not an implementation of context service " + service);
                implemented = false;
            }

        }

        if (!implemented) {
            error("Cannot ensure that the class " + classname + " is the implementation of the specified context coreServices");
        }
        
        /*
         * Add the specified context coreServices as provided specifications of the IPOJO component
         */
        String specifications = Arrays.asList(annotation.coreServices()).stream().map(service -> service.getName()).collect(Collectors.joining(",", "{", "}"));


        if (annotation.coreServices().length > 0) {
            Element provides = new Element(ProvideReferenceHandler.PROVIDES.toString(), "");
            Attribute attribute = new Attribute(ProvideReferenceHandler.SPECIFICATIONS.toString(), specifications);

            provides.addAttribute(attribute);

        /*
         * Add a static property to the component specifying all the context coreServices implemented by the entity
         */
            Element property = new Element(ProvideReferenceHandler.PROPERTY.toString(), "");

            property.addAttribute(new Attribute(ProvideReferenceHandler.NAME.toString(), ContextEntity.ENTITY_CONTEXT_SERVICES));
            property.addAttribute(new Attribute(ProvideReferenceHandler.TYPE.toString(), "string[]"));
            property.addAttribute(new Attribute(ProvideReferenceHandler.VALUE.toString(), specifications));
            property.addAttribute(new Attribute(ProvideReferenceHandler.MANDATORY.toString(), "false"));
            property.addAttribute(new Attribute(ProvideReferenceHandler.IMMUTABLE.toString(), "true"));

            provides.addElement(property);


            Attribute attributeStrategy = new Attribute(ProvideReferenceHandler.STRATEGY.toString(), ContextProvideStrategy.class.getName());
            provides.addAttribute(attributeStrategy);

            addMetadataElement(CONTEXT_PROVIDE_TYPE, provides, null);
        }


        /*
         *  Create the Entity element that will own all definitions regarding the context
         */
        Element context = new Element(HandlerReference.ENTITY_HANDLER, HandlerReference.NAMESPACE);
        addMetadataElement(CONTEXT_ENTITY_ELEMENT, context);

    }


}
