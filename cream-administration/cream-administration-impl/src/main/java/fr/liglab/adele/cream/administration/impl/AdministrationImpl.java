package fr.liglab.adele.cream.administration.impl;

import fr.liglab.adele.cream.administration.api.AdministrationService;
import fr.liglab.adele.cream.administration.api.ImmutableContextEntity;
import fr.liglab.adele.cream.administration.api.ImmutableContextState;
import fr.liglab.adele.cream.administration.api.ImmutableFunctionalExtension;
import fr.liglab.adele.cream.annotations.internal.FunctionalExtensionReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.ParseUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Component
@Instantiate
@Provides(specifications = AdministrationService.class)
public class AdministrationImpl implements AdministrationService{

    @Requires(id = "archi",specification = Architecture.class)
    List<Architecture> architectures;

    @Validate
    public void valid(){

    }

    @Invalidate
    public void invalid(){

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
    public void reconfigureContextEntityFrequency(String contextEntityId, String contextStateId, int frequency, TimeUnit unit) {
//TODO
    }

    @Override
    public void reconfigureContextEntityComposition(String contextEntityId, String functionnalExtensionId, String functionnalExtensionImplementation) {
//TODO
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

        HandlerDescription functionalTrackerHandlerDescription = instanceDescription.getHandlerDescription(HandlerReference.NAMESPACE+":"+HandlerReference.FUNCTIONAL_EXTENSION_TRACKER_HANDLER);
        List<ImmutableFunctionalExtension> functionalExtensions = new ArrayList<>();
        if (functionalTrackerHandlerDescription != null){
            functionalExtensions.addAll(getExtensionFromTrackerHandler(functionalTrackerHandlerDescription));
        }

        return new ImmutableContextEntity(entityId,entityState,contextStates,functionalExtensions);
    }

    private List<ImmutableFunctionalExtension> getExtensionFromTrackerHandler(HandlerDescription description){
        List<ImmutableFunctionalExtension> functionalExtensions = new ArrayList<>();
        Element handlerTrackerElement = description.getHandlerInfo();

        Element[] instanceElements = handlerTrackerElement.getElements("instance");
        if (instanceElements == null ){
            return functionalExtensions;
        }

        for (Element element : instanceElements){
            functionalExtensions.add(getExtensionFromFunctionalInstanceDescription(element));
        }

        return functionalExtensions;
    }


    private ImmutableFunctionalExtension getExtensionFromFunctionalInstanceDescription(Element functionalInstanceDescription){
        Element[] handlerElements = functionalInstanceDescription.getElements("handler");
        if (handlerElements == null){
            return null;
        }

        String functionnalExtensionState = getStateFromInstanceDescription(functionalInstanceDescription);
        String functionnalExtensionId="";
        List<ImmutableContextState> contextStates = new ArrayList<>();
        List<String> specs = new ArrayList<>();
        for (Element handlerElement : handlerElements){

            if ((HandlerReference.NAMESPACE+":"+HandlerReference.FUNCTIONAL_EXTENSION_LIFECYCLE_HANDLER).equals(handlerElement.getAttribute("name"))){
                // Extract behaviorId
                functionnalExtensionId = handlerElement.getAttribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_ID_CONFIG.toString());
                specs.addAll(ParseUtils.parseArraysAsList(handlerElement.getAttribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_MANAGED_SPECS_CONFIG.toString()))) ;

            }
            else if ((HandlerReference.NAMESPACE+":"+HandlerReference.FUNCTIONAL_EXTENSION_ENTITY_HANDLER).equals(handlerElement.getAttribute("name"))){
                // Extract States
                contextStates.addAll(getContextStatesFromHandlerElement(handlerElement));
            }
        }

        return new ImmutableFunctionalExtension(functionnalExtensionId,functionnalExtensionState,specs,contextStates);
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
