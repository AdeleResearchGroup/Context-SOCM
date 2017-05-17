package fr.liglab.adele.cream.administration.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aygalinc on 12/05/17.
 */
public class ImmutableContextEntity {

    private final String id;

    private final List<ImmutableContextState> contextStates;

    private final List<ImmutableFunctionalExtension> extensions;

    private final String state;

    public ImmutableContextEntity(String id,String state, List<ImmutableContextState> states, List<ImmutableFunctionalExtension> extensions) {
        this.id = id;
        this.state = state;
        this.contextStates = new ArrayList<>(states);
        this.extensions = new ArrayList<>(extensions);
    }

    public List<ImmutableFunctionalExtension> getExtensions() {
        return new ArrayList<>(extensions);
    }

    public List<ImmutableContextState> getContextStates() {
        return new ArrayList<>(contextStates);
    }

    public String getId() {
        return id;
    }

    public String getState() {
        return state;
    }
}
