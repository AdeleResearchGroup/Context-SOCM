package fr.liglab.adele.cream.administration.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aygalinc on 12/05/17.
 */
public class ImmutableFunctionalExtension {

    private final String id;

    private final String state;

    private final List<String> specifications;

    private final List<ImmutableContextState> contextStates;

    public ImmutableFunctionalExtension(String id,String state, List<String> specifications, List<ImmutableContextState> states) {
        this.id = id;
        this.state = state;
        this.specifications = new ArrayList<>(specifications);
        this.contextStates = new ArrayList<>(states);
    }

    public String getId() {
        return id;
    }

    public List<String> getSpecifications() {
        return new ArrayList<>(specifications) ;
    }

    public List<ImmutableContextState> getContextStates() {
        return new ArrayList<>(contextStates);
    }

    public String getState() {
        return state;
    }
}
