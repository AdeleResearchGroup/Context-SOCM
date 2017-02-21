package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.behavior.BehaviorProvider;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
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
public class BehaviorProviderProcessor extends AnnotationProcessor<BehaviorProvider> {

    protected static final String BEHAVIOR_CONTEXT_ENTITY_ELEMENT = HandlerReference.NAMESPACE+":"+HandlerReference.BEHAVIOR_ENTITY_HANDLER;

    protected static final String BEHAVIOR_LIFECYCLE_ELEMENT = HandlerReference.NAMESPACE+":"+HandlerReference.BEHAVIOR_LIFECYCLE_HANDLER;

    public static final String COMPONENT_TYPE = "behavior-extension";

    public BehaviorProviderProcessor(ClassLoader classReferenceLoader) {
        super(BehaviorProvider.class,classReferenceLoader);
    }

    @Override
    public void process(BehaviorProvider annotation) {
    	
    	/*
    	 * Create the corresponding root iPOJO component
    	 */
        Element component			= new Element(COMPONENT_TYPE, "");
        String classname 			= getAnnotatedClassType().getClassName();

        component.addAttribute(new Attribute("classname", classname));
        component.addAttribute(new Attribute("immediate", "true"));

        String specifications = Arrays.asList(annotation.spec()).stream().map(service -> service.getName()).collect(Collectors.joining(",","{","}"));

        component.addAttribute(new Attribute(BehaviorReference.SPECIFICATION_ATTRIBUTE_NAME,specifications));
        component.addAttribute(new Attribute(BehaviorReference.IMPLEMEMENTATION_ATTRIBUTE_NAME,classname));

        if (getRootMetadata() != null) {
            error("Multiple 'component type' annotations on the class '{%s}'.", classname);
            warn("@Entity is ignored.");
            component =getRootMetadata();
        }

        setRootMetadata(component);
        
        /*
         * Verify the annotated class implements all the context spec specified in the annotation
         */
        ClassNode clazz 	= getAnnotatedClass();
        boolean implemented = true;

        for (Class service : annotation.spec()) {
            if (!clazz.interfaces.contains(Type.getInternalName(service))) {
                error("Class " + clazz.name + " is not an implementation of context service " + service);
                implemented = false;
            }
        }

        if (! implemented) {
            error("Cannot ensure that the class " + classname + " is the implementation of the specified context spec");
        }


         /*
         *  Create the Behavior Entity element that will own all definitions regarding the context
         */
        Element behaviorEntityElement = new Element(HandlerReference.BEHAVIOR_ENTITY_HANDLER,HandlerReference.NAMESPACE);
        addMetadataElement(BEHAVIOR_CONTEXT_ENTITY_ELEMENT,behaviorEntityElement);

        /**
         * Create lyfecycle element
         */
        Element behaviorLifeCycleElement = new Element(HandlerReference.BEHAVIOR_LIFECYCLE_HANDLER,HandlerReference.NAMESPACE);
        addMetadataElement(BEHAVIOR_LIFECYCLE_ELEMENT,behaviorLifeCycleElement);


    }


}
