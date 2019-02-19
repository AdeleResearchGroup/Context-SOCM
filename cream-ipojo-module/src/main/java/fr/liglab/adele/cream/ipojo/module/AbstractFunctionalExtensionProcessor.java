package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.entity.ContextProvideStrategy;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

import fr.liglab.adele.cream.annotations.internal.FunctionalExtensionReference;
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
public abstract class AbstractFunctionalExtensionProcessor<A extends Annotation> extends AnnotationProcessor<A> {

    protected static final String BEHAVIOR_ELEMENT = HandlerReference.NAMESPACE + ":" + HandlerReference.FUNCTIONAL_EXTENSION_TRACKER_HANDLER;

    protected AbstractFunctionalExtensionProcessor(Class<A> annotationType, ClassLoader classReferenceLoader) {
        super(annotationType, classReferenceLoader);
    }

    protected void buildSubBehaviorElement(FunctionalExtension annotation) {
        if (getMetadataElement(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_INDIVIDUAL_ELEMENT_NAME + ":" + annotation.id()) != null) {
            error("FunctionalExtension id must be unique. Duplicate id : " + annotation.id());
        }

        for (Class<?> service : annotation.contextServices()) {
            checkNoSpecificationRedundancy(service.getName());
            addSpecToProvideElement(service.getName());
        }

        Element behaviorIndividualElement = new Element(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_INDIVIDUAL_ELEMENT_NAME.toString(), "");
        String specifications = Arrays.asList(annotation.contextServices()).stream().map(service -> service.getName()).collect(Collectors.joining(",", "{", "}"));
        Attribute specAttr = new Attribute(FunctionalExtensionReference.SPECIFICATION_ATTRIBUTE_NAME.toString(), specifications);
        Attribute implAttr = new Attribute(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString(), annotation.implementation().getName());
        Attribute id = new Attribute(FunctionalExtensionReference.ID_ATTRIBUTE_NAME.toString(), annotation.id());
        Attribute mandatory = new Attribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_MANDATORY_ATTRIBUTE_NAME.toString(), String.valueOf(annotation.mandatory()));
        behaviorIndividualElement.addAttribute(specAttr);
        behaviorIndividualElement.addAttribute(implAttr);
        behaviorIndividualElement.addAttribute(id);
        behaviorIndividualElement.addAttribute(mandatory);

        addMetadataElement(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_INDIVIDUAL_ELEMENT_NAME + ":" + annotation.id(), behaviorIndividualElement, getBehaviorParentElement());

    }

    private String getBehaviorParentElement() {
        Element behaviorElement = getMetadataElement(BEHAVIOR_ELEMENT);

        if (behaviorElement == null) {
            behaviorElement = new Element(HandlerReference.FUNCTIONAL_EXTENSION_TRACKER_HANDLER, HandlerReference.NAMESPACE);
            addMetadataElement(BEHAVIOR_ELEMENT, behaviorElement);
        }
        return BEHAVIOR_ELEMENT;
    }

    private void checkNoSpecificationRedundancy(String specification) {
        Element element = getMetadataElement(getBehaviorParentElement());
        Element[] behaviorElements = element.getElements();
        for (Element individualElement : behaviorElements) {

            String[] specifications = ParseUtils.parseArrays(individualElement.getAttribute(FunctionalExtensionReference.SPECIFICATION_ATTRIBUTE_NAME.toString()));

            for (String spec : specifications) {
                if (specification.equals(spec)) {
                    error(" Error in behavior annotation. Duplicate behavior for specification " + specification);
                }
            }

        }
    }

    private void addSpecToProvideElement(String behaviorSpecification) {
        Element provideElement = getProvideElement();
        String newSpecs;
        if (provideElement.getAttribute(ProvideReferenceHandler.SPECIFICATIONS.toString()) == null || provideElement.getAttribute(ProvideReferenceHandler.SPECIFICATIONS.toString()).length() == 0) {
            newSpecs = "{" + behaviorSpecification + "}";
        } else {
            String specs = provideElement.getAttribute(ProvideReferenceHandler.SPECIFICATIONS.toString());
            String temp = specs.substring(0, specs.length() - 1);
            newSpecs = temp + "," + behaviorSpecification + "}";
            Attribute newSpecAttribute = new Attribute(ProvideReferenceHandler.SPECIFICATIONS.toString(), newSpecs);
            provideElement.addAttribute(newSpecAttribute);
        }

        Attribute newSpecAttribute = new Attribute(ProvideReferenceHandler.SPECIFICATIONS.toString(), newSpecs);
        provideElement.addAttribute(newSpecAttribute);

    }

    private Element getProvideElement() {
        Element providesElement = getMetadataElement(ContextEntityProcessor.CONTEXT_PROVIDE_TYPE);
        if (providesElement == null) {
            Element provides = new Element(ProvideReferenceHandler.PROVIDES.toString(), "");

        /*
         * Add a static property to the component specifying all the context spec implemented by the entity
         */
            Element property = new Element(ProvideReferenceHandler.PROPERTY.toString(), "");

            property.addAttribute(new Attribute(ProvideReferenceHandler.NAME.toString(), ContextEntity.ENTITY_CONTEXT_SERVICES));
            property.addAttribute(new Attribute(ProvideReferenceHandler.TYPE.toString(), "string[]"));
            property.addAttribute(new Attribute(ProvideReferenceHandler.MANDATORY.toString(), "false"));
            property.addAttribute(new Attribute(ProvideReferenceHandler.IMMUTABLE.toString(), "true"));

            provides.addElement(property);


            Attribute attributeStrategy = new Attribute(ProvideReferenceHandler.STRATEGY.toString(),  ContextProvideStrategy.class.getName());
            provides.addAttribute(attributeStrategy);

            addMetadataElement(ContextEntityProcessor.CONTEXT_PROVIDE_TYPE, provides, null);
            return provides;
        }
        return providesElement;

    }
}
