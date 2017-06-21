package fr.liglab.adele.cream.administration.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aygalinc on 21/06/17.
 */
public class ImmutableRelation {

    private final List<String> sourcesId;

    private final String state;

    public ImmutableRelation(List<String> sourcesId,String state) {
        this.sourcesId = new ArrayList<>(sourcesId);
        this.state = state;
    }

    public List<String> getSourcesId() {
        return new ArrayList<>(sourcesId);
    }

    public String getState() {
        return state;
    }
}
