package fr.liglab.adele.cream.administration.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aygalinc on 21/06/17.
 */
public class ImmutableCore {

    private final List<ImmutableRelation> relations;

    private final List<ImmutableContextState> contextStates;

    private final List<String> implementedSpecifications;

    private final String implementation;

    public ImmutableCore(List<ImmutableRelation> relations, List<ImmutableContextState> contextStates, List<String> implementedSpecifications,String implementation) {
        this.relations = new ArrayList<>(relations);
        this.contextStates = new ArrayList<>(contextStates);
        this.implementedSpecifications = new ArrayList<>(implementedSpecifications);
        this.implementation=implementation;
    }

    public List<ImmutableContextState> getContextStates() {
        return new ArrayList<>(contextStates);
    }

    public List<String> getImplementedSpecifications() {
        return new ArrayList<>(implementedSpecifications);
    }

    public List<ImmutableRelation> getRelations() {
        return new ArrayList<>(relations);
    }

    public String getImplementation() {
        return implementation;
    }
}
