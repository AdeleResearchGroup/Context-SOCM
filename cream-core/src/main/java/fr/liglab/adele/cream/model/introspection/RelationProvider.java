package fr.liglab.adele.cream.model.introspection;

import java.util.Set;

/**
 * Created by Eva on 14/12/2015.
 */
public interface RelationProvider {

    public String getName();

    public Set<String> getProvidedRelations();

    public boolean isEnabled(String relation);

    public boolean enable(String relation);

    public boolean disable(String relation);

    public Set<String> getInstances(String relation, boolean includePending);

    public boolean deleteInstances(String relation, boolean onlyPending);

    public Set<String> getPotentiallyProvidedRelationServices(String relation);

    public Set<String> getPotentiallyProvidedRelationServices();

    public Set<String> getPotentiallyRequiredServices(String relation);

    public Set<String> getPotentiallyRequiredServices();
}
