package fr.liglab.adele.cream.administration.impl;

import fr.liglab.adele.cream.administration.api.*;
import fr.liglab.adele.cream.annotations.internal.FunctionalExtensionReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.annotations.internal.ReservedCreamValueReference;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.ParseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Component(immediate = true)
@Instantiate(name = "fr.liglab.adele.cream.administration.impl.AdministrationImpl-0")
@Provides(specifications = AdministrationService.class)
public class AdministrationImpl implements AdministrationService{

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AdministrationImpl.class);

    private final Map<String,Architecture>  instanceNameToArchitecture = new ConcurrentHashMap<>();

    @Requires(id = "archi",specification = Architecture.class,optional = true)
    List<Architecture> architectures;

    @Bind(id = "archi")
    public void bindArchitecture(Architecture architecture){
        instanceNameToArchitecture.put(architecture.getInstanceDescription().getName(),architecture);
    }


    @Unbind(id = "archi")
    public void unbindArchitecture(Architecture architecture){
        String name = architecture.getInstanceDescription().getName();

        instanceNameToArchitecture.remove(name);
    }

    @Invalidate
    public void stop(){
        instanceNameToArchitecture.clear();
    }

    @Override
    public Set<ImmutableContextEntity> getContextEntities() {
        return convertArchitecturesToImmutableContextEntities(architectures);
    }

    @Override
    public ImmutableContextEntity getContextEntity(String id) {
        for (ImmutableContextEntity contextEntity : convertArchitecturesToImmutableContextEntities(architectures)){
            if (contextEntity.getId().equals(id)){
                return contextEntity;
            }
        }
        return null;
    }

    @Override
    public void reconfigureContextEntityFrequency(String contextEntityId, String contextStateId, long frequency, TimeUnit unit) {
        if (contextEntityId == null || contextStateId == null || unit == null){
            LOG.warn("Cannot try to reconfigure, one of the parameter is false ");
            return;
        }

        for (Architecture architecture : architectures){
            InstanceDescription instanceDescription = architecture.getInstanceDescription();
            HandlerDescription entityHandlerDescription = instanceDescription.getHandlerDescription(HandlerReference.NAMESPACE+":"+HandlerReference.ENTITY_HANDLER);

            if (entityHandlerDescription == null){
                continue;
            }

            String entityId = getContextEntityIdFromEntityHandlerDescription(entityHandlerDescription);
            if (contextEntityId.equals(entityId)){
                Hashtable configuration = new Hashtable();

                Map stateMap = new HashMap();
                Map frequencyParam = new HashMap();
                frequencyParam.put(ReservedCreamValueReference.RECONFIGURATION_FREQUENCY_UNIT.toString(), unit );
                frequencyParam.put(ReservedCreamValueReference.RECONFIGURATION_FREQUENCY_PERIOD.toString(),frequency);
                stateMap.put(contextStateId,frequencyParam);
                configuration.put(ReservedCreamValueReference.RECONFIGURATION_FREQUENCY.toString(),stateMap);

                architecture.getInstanceDescription().getInstance().reconfigure(configuration);
            }
        }

    }

    @Override
    public void reconfigureContextEntityComposition(String contextEntityId, String functionalExtensionId, String functionalExtensionImplementation) {
        if (contextEntityId == null || functionalExtensionId == null || functionalExtensionImplementation == null){
            LOG.warn("Cannot try to reconfigure, one of the parameter is false ");
            return;
        }

        for (Architecture architecture : architectures){
            InstanceDescription instanceDescription = architecture.getInstanceDescription();
            HandlerDescription entityHandlerDescription = instanceDescription.getHandlerDescription(HandlerReference.NAMESPACE+":"+HandlerReference.ENTITY_HANDLER);

            if (entityHandlerDescription == null){
                continue;
            }

            String entityId = getContextEntityIdFromEntityHandlerDescription(entityHandlerDescription);
            if (contextEntityId.equals(entityId)){
                Hashtable configuration = new Hashtable();

                Map compositionMap = new HashMap();

                compositionMap.put(FunctionalExtensionReference.ID_ATTRIBUTE_NAME.toString(), functionalExtensionId );
                compositionMap.put(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString(),functionalExtensionImplementation);
                configuration.put(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_RECONFIGURATION.toString(),compositionMap);

                architecture.getInstanceDescription().getInstance().reconfigure(configuration);
            }
        }
    }

    private Set<ImmutableContextEntity> convertArchitecturesToImmutableContextEntities(Collection<Architecture> architectures){
        Set<ImmutableContextEntity> immutableContextEntities = new HashSet<>();

        if (architectures == null){
            return immutableContextEntities;
        }

        for (Architecture architecture : architectures){
            ImmutableContextEntity contextEntity = convertArchitectureToImmutableContextEntity(architecture);
            if (contextEntity != null){
                immutableContextEntities.add(contextEntity);
            }
        }
        return immutableContextEntities;
    }

    private ImmutableContextEntity convertArchitectureToImmutableContextEntity(Architecture architecture){
        InstanceDescription instanceDescription = architecture.getInstanceDescription();
        HandlerDescription entityHandlerDescription = instanceDescription.getHandlerDescription(HandlerReference.NAMESPACE+":"+HandlerReference.ENTITY_HANDLER);

        if (entityHandlerDescription == null){
            return null;
        }

        String entityId = getContextEntityIdFromEntityHandlerDescription(entityHandlerDescription);
        List<ImmutableContextState> contextStates = getContextStatesFromEntityHandlerDescription(entityHandlerDescription);
        String entityState = getStateFromInstanceDescription(instanceDescription.getDescription());
        List<String> implementedSpecs = getImplementedSpecificationsFromEntityHandlerDescription(entityHandlerDescription.getHandlerInfo());
        String implementation = instanceDescription.getDescription().getAttribute("component.type");

        HandlerDescription functionalTrackerHandlerDescription = instanceDescription.getHandlerDescription(HandlerReference.NAMESPACE+":"+HandlerReference.FUNCTIONAL_EXTENSION_TRACKER_HANDLER);
        List<ImmutableFunctionalExtension> functionalExtensions = new ArrayList<>();
        if (functionalTrackerHandlerDescription != null){
            functionalExtensions.addAll(getExtensionFromTrackerHandler(functionalTrackerHandlerDescription));
        }

        List<ImmutableRelation> relations = getListOfRelationFromRequireHandlerDescription(instanceDescription.getHandlerDescription("org.apache.felix.ipojo:requires"));
        ImmutableCore core = new ImmutableCore(relations,contextStates,implementedSpecs,implementation);

        return new ImmutableContextEntity(entityId,entityState,core,functionalExtensions);

    }

    private List<ImmutableRelation> getListOfRelationFromRequireHandlerDescription(HandlerDescription requireHandlerDescription){

        if (requireHandlerDescription == null){
            return new ArrayList<>();
        }

        return getListOfRelationFromRequireHandlerElement(requireHandlerDescription.getHandlerInfo());

    }

    private List<ImmutableRelation> getListOfRelationFromRequireHandlerElement(Element requireHandlerElement) {
        List<ImmutableRelation> relations = new ArrayList<>();
        Element[] requiresElements = requireHandlerElement.getElements("requires");

        if (requiresElements == null){
            return relations;
        }

        for (Element requires :requiresElements){
            String state = requires.getAttribute("state");

            Element[] usesElements = requires.getElements("selected");

            if (usesElements == null){
                continue;
            }

            List<String> sourcesId = new ArrayList<>();

            for (Element uses : usesElements){
                String instanceName = uses.getAttribute("instance.name");
                if (instanceName == null){
                    continue;
                }

                Architecture sourceArchi = instanceNameToArchitecture.get(instanceName);
                if (sourceArchi == null){
                    continue;
                }
                InstanceDescription instanceDescription = sourceArchi.getInstanceDescription();
                HandlerDescription entityHandlerDescription = instanceDescription.getHandlerDescription(HandlerReference.NAMESPACE+":"+HandlerReference.ENTITY_HANDLER);
                if (entityHandlerDescription == null){
                    return null;
                }
                sourcesId.add(getContextEntityIdFromEntityHandlerDescription(entityHandlerDescription));
            }

            if (sourcesId.isEmpty()){
                continue;
            }
            relations.add(new ImmutableRelation(sourcesId,state));
        }
        return relations;
    }

        private List<ImmutableFunctionalExtension> getExtensionFromTrackerHandler(HandlerDescription description){
        List<ImmutableFunctionalExtension> functionalExtensions = new ArrayList<>();
        Element handlerTrackerElement = description.getHandlerInfo();

        Element[] functionalExtensionElements = handlerTrackerElement.getElements(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_INDIVIDUAL_ELEMENT_NAME.toString());
        if (functionalExtensionElements == null ){
            return functionalExtensions;
        }

        for (Element functionalExtensionElement : functionalExtensionElements){
            functionalExtensions.add(getExtensionFromFunctionalInstanceDescription(functionalExtensionElement));
        }

        return functionalExtensions;
    }


    private ImmutableFunctionalExtension getExtensionFromFunctionalInstanceDescription(Element functionalExtensionElement){
        String functionalExtensionId= functionalExtensionElement.getAttribute(FunctionalExtensionReference.ID_ATTRIBUTE_NAME.toString());

        List<String> managedSpecs = ParseUtils.parseArraysAsList(functionalExtensionElement.getAttribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_MANAGED_SPECS_CONFIG.toString()));
        String isMandatory = functionalExtensionElement.getAttribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_MANDATORY_ATTRIBUTE_NAME.toString());
        List<String> alternativeConfigurations = ParseUtils.parseArraysAsList(functionalExtensionElement.getAttribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_ALTERNATIVE_CONFIGURATION.toString()));
        String isInstantiate = functionalExtensionElement.getAttribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_IS_INSTANTIATE.toString());
        String selectedImplem = functionalExtensionElement.getAttribute(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString());

        String functionnalExtensionState = "";
        List<ImmutableContextState> contextStates = new ArrayList<>();
        List<String> implementedSpecs = new ArrayList<>();
        List<ImmutableRelation> relations = new ArrayList<>();
        if ("true".equals(isInstantiate)){
            Element[] extensionInstanceElements = functionalExtensionElement.getElements("instance");
            if (extensionInstanceElements == null || extensionInstanceElements.length == 0){
                throw new RuntimeException("isInstantiate Element is set to true but no instance element is provided");
            }

            for (Element extensionInstanceElement : extensionInstanceElements){
                functionnalExtensionState = getStateFromInstanceDescription(extensionInstanceElement);


                Element[] handlerElements = extensionInstanceElement.getElements("handler");
                if (handlerElements == null){
                    return null;
                }
                for (Element handlerElement : handlerElements){

                    if ((HandlerReference.NAMESPACE+":"+HandlerReference.FUNCTIONAL_EXTENSION_ENTITY_HANDLER).equals(handlerElement.getAttribute("name"))){
                        // Extract States
                        contextStates.addAll(getContextStatesFromHandlerElement(handlerElement));
                        implementedSpecs.addAll(getImplementedSpecificationsFromEntityHandlerDescription(handlerElement));
                    }else if ("org.apache.felix.ipojo:requires".equals(handlerElement.getAttribute("name"))){
                        relations = getListOfRelationFromRequireHandlerElement(handlerElement);
                    }
                }

            }
        }




        return new ImmutableFunctionalExtension(functionalExtensionId,functionnalExtensionState,implementedSpecs,managedSpecs,alternativeConfigurations,contextStates,isInstantiate,isMandatory,relations,selectedImplem);
    }

    private List<String> getImplementedSpecificationsFromEntityHandlerDescription(Element instanceDescription){
        return ParseUtils.parseArraysAsList(instanceDescription.getAttribute("context.specifications"));
    }

    private String getStateFromInstanceDescription(Element instanceDescription){
        return instanceDescription.getAttribute("state");
    }

    private List<ImmutableContextState> getContextStatesFromEntityHandlerDescription(HandlerDescription description){
        return getContextStatesFromHandlerElement(description.getHandlerInfo());
    }

    private List<ImmutableContextState> getContextStatesFromHandlerElement(Element handlerElement){

        List<ImmutableContextState> contextStates = new ArrayList<>();

        Element[] stateElements = handlerElement.getElements("state");

        for (Element state : stateElements){

            String id = state.getAttribute("id");

            if (!"context.entity.id".equals(id)){
                contextStates.add(new ImmutableContextState(id,state.getAttribute("value"),state.getAttribute("period"),state.getAttribute("unit")));
            }

        }
        return contextStates;
    }


    private String getContextEntityIdFromEntityHandlerDescription(HandlerDescription description){
        Element handlerInfo = description.getHandlerInfo();
        Element[] stateElements = handlerInfo.getElements("state");
        if (stateElements == null){
            //Normaly it's dead code
            return null;
        }

        for (Element state:stateElements){

            if ("context.entity.id".equals(state.getAttribute("id"))){
                return state.getAttribute("value");
            }
        }

        //Dead Code
        return null;
    }
}
