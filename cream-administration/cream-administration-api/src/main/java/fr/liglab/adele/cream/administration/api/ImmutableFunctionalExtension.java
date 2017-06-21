package fr.liglab.adele.cream.administration.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aygalinc on 12/05/17.
 */
public class ImmutableFunctionalExtension {

    private final String id;

    private final String state;

    private final String isInstantiate;

    private final String isMandatory;

    private final String selectedImplementation;

    private final List<String> managedSpecifications;

    private final List<String> implementedSpecifications;

    private final List<String> alternativeConfigurations;

    private final List<ImmutableContextState> contextStates;

    private final List<ImmutableRelation> relations;

    public ImmutableFunctionalExtension(String id,String state,List<String> implementedSpecifications,
                                        List<String> managedSpecifications,List<String> alternativeConfigurations, List<ImmutableContextState> states,
                                        String isInstantiate,String isMandatory,List<ImmutableRelation> relations,String selectedImplementation) {
        this.id = id;
        this.state = state;
        this.managedSpecifications = new ArrayList<>(managedSpecifications);
        this.implementedSpecifications = new ArrayList<>(implementedSpecifications);
        this.contextStates = new ArrayList<>(states);
        this.isInstantiate = isInstantiate;
        this.isMandatory = isMandatory;
        this.alternativeConfigurations = alternativeConfigurations;
        this.relations = relations;
        this.selectedImplementation = selectedImplementation;
    }

    public String getId() {
        return id;
    }

    public List<String> getManagedSpecifications() {
        return new ArrayList<>(managedSpecifications) ;
    }

    public List<String> getImplementedSpecifications() {
        return new ArrayList<>(implementedSpecifications) ;
    }

    public List<ImmutableContextState> getContextStates() {
        return new ArrayList<>(contextStates);
    }

    public String getState() {
        return state;
    }

    public String isInstantiate() {
        return isInstantiate;
    }

    public String isMandatory() {
        return isMandatory;
    }

    public List<String> getAlternativeConfigurations() {
        return alternativeConfigurations;
    }

    public List<ImmutableRelation> getRelations() {
        return new ArrayList<>(relations);
    }

    public String getSelectedImplementation() {
        return selectedImplementation;
    }
}
