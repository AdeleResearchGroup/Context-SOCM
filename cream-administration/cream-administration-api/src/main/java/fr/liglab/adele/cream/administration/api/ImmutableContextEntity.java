package fr.liglab.adele.cream.administration.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aygalinc on 12/05/17.
 */
public class ImmutableContextEntity {

    private final String id;

   private final ImmutableCore core;

    private final List<ImmutableFunctionalExtension> extensions;

    private final String state;

    public ImmutableContextEntity(String id,String state,ImmutableCore core, List<ImmutableFunctionalExtension> extensions) {
        this.id = id;
        this.state = state;
        this.extensions = new ArrayList<>(extensions);
        this.core = core;
    }

    public List<ImmutableFunctionalExtension> getExtensions() {
        return new ArrayList<>(extensions);
    }

    public ImmutableCore getCore() {
        return core;
    }

    public String getId() {
        return id;
    }

    public String getState() {
        return state;
    }
}
