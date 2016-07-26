package fr.liglab.adele.cream.ipojo.module;

import java.util.Arrays;
import java.util.stream.Collectors;

import fr.liglab.adele.cream.annotations.entity.StrategyReference;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;

/**
 * Created by aygalinc on 14/01/16.
 */
public class ContextEntityProcessor extends AnnotationProcessor<ContextEntity> {

    protected static final String CONTEXT_ENTITY_ELEMENT = HandlerReference.NAMESPACE+":"+HandlerReference.ENTITY_HANDLER;

    public static final String COMPONENT_TYPE = "context-component";

    public static final String CONTEXT_PROVIDE_TYPE = "context-component";

    public ContextEntityProcessor(ClassLoader classReferenceLoader) {
		super(ContextEntity.class,classReferenceLoader);
	}
    
    @Override
	public void process(ContextEntity annotation) {
    	
    	/*
    	 * Create the corresponding root iPOJO component
    	 */
    	Element component			= new Element(COMPONENT_TYPE, "");
        String classname 			= getAnnotatedClassType().getClassName();

    	component.addAttribute(new Attribute("classname", classname));
    	component.addAttribute(new Attribute("immediate", "true"));
    	
        if (getRootMetadata() != null) {
            error("Multiple 'component type' annotations on the class '{%s}'.", classname);
            warn("@Entity is ignored.");
            component =getRootMetadata();
        }
        
        setRootMetadata(component);
        
        /*
         * Verify the annotated class implements all the context services specified in the annotation
         */
        ClassNode clazz 	= getAnnotatedClass();
        boolean implemented = true;
        
        for (Class<?> service : annotation.services()) {
    		
        	if (!clazz.interfaces.contains(Type.getInternalName(service))) {
   				error("Class " + clazz.name + " is not an implementation of entity service " + service);
    			implemented = false;
            }
			
		}

        if (! implemented) {
        	error("Cannot ensure that the class " + classname + " is the implementation of the specified context services");
        }
        
        /*
         * Add the specified context services as provided specifications of the IPOJO component
         */
        String specifications = Arrays.asList(annotation.services()).stream().map(service -> service.getName()).collect(Collectors.joining(",","{","}"));
        
        Element provides = new Element("provides","");
        if (annotation.services().length > 0) {
            Attribute attribute = new Attribute("specifications", specifications);
            Attribute attributeStrategy = new Attribute("strategy", StrategyReference.STRATEGY_PATH);
            provides.addAttribute(attribute);
            provides.addAttribute(attributeStrategy);
        }
        
      //  addMetadataElement(provides);
        addMetadataElement(CONTEXT_PROVIDE_TYPE,provides,null);
        /*
         * Add a static property to the component specifying all the context services implemented by the entity
         */
        Element property  = new Element("property", "");
        
        property.addAttribute(new Attribute("name", ContextEntity.ENTITY_CONTEXT_SERVICES));
        property.addAttribute(new Attribute("type", "string[]"));
        property.addAttribute(new Attribute("value", specifications));
        property.addAttribute(new Attribute("mandatory", "false"));
        property.addAttribute(new Attribute("immutable", "true"));
        
        provides.addElement(property);
        
        /*
         *  Create the Entity element that will own all definitions regarding the context
         */
        Element context = new Element(HandlerReference.ENTITY_HANDLER,HandlerReference.NAMESPACE);
        addMetadataElement(CONTEXT_ENTITY_ELEMENT,context);
        
   }


}
