package fr.liglab.adele.cream.administration.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aygalinc on 12/05/17.
 */
public class ImmutableFunctionalExtension {

    private final String id;

    private final String state;

    private final List<String> managedSpecifications;

    private final List<String> implementedSpecifications;

    private final List<ImmutableContextState> contextStates;

    public ImmutableFunctionalExtension(String id,String state,List<String> implementedSpecifications, List<String> managedSpecifications, List<ImmutableContextState> states) {
        this.id = id;
        this.state = state;
        this.managedSpecifications = new ArrayList<>(managedSpecifications);
        this.implementedSpecifications = new ArrayList<>(implementedSpecifications);
        this.contextStates = new ArrayList<>(states);
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
}
