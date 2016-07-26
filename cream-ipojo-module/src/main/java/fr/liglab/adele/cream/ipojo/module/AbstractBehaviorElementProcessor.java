package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import java.lang.annotation.Annotation;

/**
 * Created by aygalinc on 26/07/16.
 */
public abstract class AbstractBehaviorElementProcessor<A extends Annotation> extends AnnotationProcessor<A> {

    protected static final String BEHAVIOR_ELEMENT = HandlerReference.NAMESPACE +":"+ HandlerReference.BEHAVIOR_MANAGER_HANDLER;

    protected AbstractBehaviorElementProcessor(Class annotationType, ClassLoader classReferenceLoader) {
        super(annotationType, classReferenceLoader);
    }

    protected void buildSubBehaviorElement(Behavior annotation){
        Element behaviorIndividualElement = new Element(BehaviorReference.BEHAVIOR_INDIVIDUAL_ELEMENT_NAME,"");
        Attribute specAttr = new Attribute(BehaviorReference.SPECIFICATION_ATTRIBUTE_NAME,annotation.spec().getName());
        Attribute implAttr = new Attribute(BehaviorReference.IMPLEMEMENTATION_ATTRIBUTE_NAME,annotation.implem().getName());
        Attribute id = new Attribute(BehaviorReference.ID_ATTRIBUTE_NAME,annotation.id());
        behaviorIndividualElement.addAttribute(specAttr);
        behaviorIndividualElement.addAttribute(implAttr);
        behaviorIndividualElement.addAttribute(id);

        addMetadataElement(BehaviorReference.BEHAVIOR_INDIVIDUAL_ELEMENT_NAME,behaviorIndividualElement,getBehaviorParentElement());
        addSpecToProvideElement(annotation.spec().getName());
    }

    private String getBehaviorParentElement(){
        Element behaviorElement = getMetadataElement(BEHAVIOR_ELEMENT);

        if (behaviorElement == null){
            behaviorElement = new Element(HandlerReference.BEHAVIOR_MANAGER_HANDLER,HandlerReference.NAMESPACE);
            addMetadataElement(BEHAVIOR_ELEMENT,behaviorElement);
        }
        return BEHAVIOR_ELEMENT;
    }

    private void addSpecToProvideElement(String behaviorSpecification){
        Element providesElements = getMetadataElement(ContextEntityProcessor.CONTEXT_PROVIDE_TYPE);
        String specs = new String(providesElements.getAttribute("specifications"));
        String temp = specs.substring(0,specs.length()-1);
        String newSpecs = temp + ","+behaviorSpecification+"}";
        Attribute newSpecAttribute= new Attribute("specifications",newSpecs);
        providesElements.addAttribute(newSpecAttribute);
    }
}
