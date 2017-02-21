package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.entity.StrategyReference;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.ParseUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by aygalinc on 26/07/16.
 */
public abstract class AbstractBehaviorElementProcessor<A extends Annotation> extends AnnotationProcessor<A> {

    protected static final String BEHAVIOR_ELEMENT = HandlerReference.NAMESPACE +":"+ HandlerReference.BEHAVIOR_MANAGER_HANDLER;

    protected AbstractBehaviorElementProcessor(Class annotationType, ClassLoader classReferenceLoader) {
        super(annotationType, classReferenceLoader);
    }

    protected void buildSubBehaviorElement(Behavior annotation){
        if (getMetadataElement(BehaviorReference.BEHAVIOR_INDIVIDUAL_ELEMENT_NAME+":"+annotation.id()) != null){
            error("Behavior id must be unique. Duplicate id : " + annotation.id());
        }

        for (Class service : annotation.contextServices()){
            checkNoSpecificationRedundancy(service.getName());
            addSpecToProvideElement(service.getName());
        }

        Element behaviorIndividualElement = new Element(BehaviorReference.BEHAVIOR_INDIVIDUAL_ELEMENT_NAME,"");
        String specifications = Arrays.asList(annotation.contextServices()).stream().map(service -> service.getName()).collect(Collectors.joining(",","{","}"));
        Attribute specAttr = new Attribute(BehaviorReference.SPECIFICATION_ATTRIBUTE_NAME,specifications);
        Attribute implAttr = new Attribute(BehaviorReference.IMPLEMEMENTATION_ATTRIBUTE_NAME,annotation.implem().getName());
        Attribute id = new Attribute(BehaviorReference.ID_ATTRIBUTE_NAME,annotation.id());
        Attribute mandatory = new Attribute(BehaviorReference.BEHAVIOR_MANDATORY_ATTRIBUTE_NAME,String.valueOf(annotation.mandatory()));
        behaviorIndividualElement.addAttribute(specAttr);
        behaviorIndividualElement.addAttribute(implAttr);
        behaviorIndividualElement.addAttribute(id);
        behaviorIndividualElement.addAttribute(mandatory);

        addMetadataElement(BehaviorReference.BEHAVIOR_INDIVIDUAL_ELEMENT_NAME+":"+annotation.id(),behaviorIndividualElement,getBehaviorParentElement());

    }

    private String getBehaviorParentElement(){
        Element behaviorElement = getMetadataElement(BEHAVIOR_ELEMENT);

        if (behaviorElement == null){
            behaviorElement = new Element(HandlerReference.BEHAVIOR_MANAGER_HANDLER,HandlerReference.NAMESPACE);
            addMetadataElement(BEHAVIOR_ELEMENT,behaviorElement);
        }
        return BEHAVIOR_ELEMENT;
    }

    private void checkNoSpecificationRedundancy(String specification){
        Element element = getMetadataElement(getBehaviorParentElement());
        Element[] behaviorElements = element.getElements();
        for (Element individualElement : behaviorElements){

            String[] specifications = ParseUtils.parseArrays(individualElement.getAttribute(BehaviorReference.SPECIFICATION_ATTRIBUTE_NAME));

            for (String spec: specifications) {
                if (specification.equals(spec)) {
                    error(" Error in behavior annotation. Duplicate behavior for specification " + specification);
                }
            }

        }
    }

    private void addSpecToProvideElement(String behaviorSpecification){
        Element provideElement = getProvideElement();
        String newSpecs;
        if (provideElement.getAttribute("specifications") == null || provideElement.getAttribute("specifications").length() == 0 ){
            newSpecs = "{"+behaviorSpecification+"}";
        }else {
            String specs = new String(provideElement.getAttribute("specifications"));
            String temp = specs.substring(0,specs.length()-1);
            newSpecs = temp + ","+behaviorSpecification+"}";
            Attribute newSpecAttribute= new Attribute("specifications",newSpecs);
            provideElement.addAttribute(newSpecAttribute);
        }

        Attribute newSpecAttribute= new Attribute("specifications",newSpecs);
        provideElement.addAttribute(newSpecAttribute);

    }

    private Element getProvideElement(){
        Element providesElement = getMetadataElement(ContextEntityProcessor.CONTEXT_PROVIDE_TYPE);
        if (providesElement == null){
            Element provides = new Element("provides","");

        /*
         * Add a static property to the component specifying all the context spec implemented by the entity
         */
            Element property  = new Element("property", "");

            property.addAttribute(new Attribute("name", ContextEntity.ENTITY_CONTEXT_SERVICES));
            property.addAttribute(new Attribute("type", "string[]"));
            property.addAttribute(new Attribute("mandatory", "false"));
            property.addAttribute(new Attribute("immutable", "true"));

            provides.addElement(property);


            Attribute attributeStrategy = new Attribute("strategy", StrategyReference.STRATEGY_PATH);
            provides.addAttribute(attributeStrategy);

            addMetadataElement(ContextEntityProcessor.CONTEXT_PROVIDE_TYPE,provides,null);
            return provides;
        }
        return providesElement;

    }
}
