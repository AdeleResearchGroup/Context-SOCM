package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;
import fr.liglab.adele.cream.annotations.internal.FunctionalExtensionReference;
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
public class FunctionalExtenderProcessor extends AnnotationProcessor<FunctionalExtender> {

    public static final String FUNCTIONAL_EXTENSION_COMPONENT_TYPE = "functional-extension";
    protected static final String FUNCTIONAL_EXTENSION_CONTEXT_ENTITY_ELEMENT = HandlerReference.NAMESPACE + ":" + HandlerReference.FUNCTIONAL_EXTENSION_ENTITY_HANDLER;
    protected static final String FUNCTIONAL_EXTENSION_LIFECYCLE_ELEMENT = HandlerReference.NAMESPACE + ":" + HandlerReference.FUNCTIONAL_EXTENSION_LIFECYCLE_HANDLER;

    public FunctionalExtenderProcessor(ClassLoader classReferenceLoader) {
        super(FunctionalExtender.class, classReferenceLoader);
    }

    @Override
    public void process(FunctionalExtender annotation) {

    	/*
    	 * Create the corresponding root iPOJO component
    	 */
        Element component = new Element(FUNCTIONAL_EXTENSION_COMPONENT_TYPE, "");
        String classname = getAnnotatedClassType().getClassName();

        component.addAttribute(new Attribute("classname", classname));
        component.addAttribute(new Attribute("immediate", "true"));

        String specifications = Arrays.asList(annotation.contextServices()).stream().map(service -> service.getName()).collect(Collectors.joining(",", "{", "}"));

        component.addAttribute(new Attribute(FunctionalExtensionReference.SPECIFICATION_ATTRIBUTE_NAME.toString(), specifications));
        component.addAttribute(new Attribute(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString(), classname));

        if (getRootMetadata() != null) {
            error("Multiple 'component type' annotations on the class '{%s}'.", classname);
            warn("@Entity is ignored.");
            component = getRootMetadata();
        }

        setRootMetadata(component);
        
        /*
         * Verify the annotated class implements all the context spec specified in the annotation
         */
        ClassNode clazz = getAnnotatedClass();
        boolean implemented = true;

        for (Class service : annotation.contextServices()) {
            if (!clazz.interfaces.contains(Type.getInternalName(service))) {
                error("Class " + clazz.name + " is not an implementation of context service " + service);
                implemented = false;
            }
        }

        if (!implemented) {
            error("Cannot ensure that the class " + classname + " is the implementation of the specified context spec");
        }


         /*
         *  Create the FunctionalExtension Entity element that will own all definitions regarding the context
         */
        Element behaviorEntityElement = new Element(HandlerReference.FUNCTIONAL_EXTENSION_ENTITY_HANDLER, HandlerReference.NAMESPACE);
        addMetadataElement(FUNCTIONAL_EXTENSION_CONTEXT_ENTITY_ELEMENT, behaviorEntityElement);

        /**
         * Create lyfecycle element
         */
        Element behaviorLifeCycleElement = new Element(HandlerReference.FUNCTIONAL_EXTENSION_LIFECYCLE_HANDLER, HandlerReference.NAMESPACE);
        addMetadataElement(FUNCTIONAL_EXTENSION_LIFECYCLE_ELEMENT, behaviorLifeCycleElement);


    }


}
