package fr.liglab.adele.cream.model.introspection;

import fr.liglab.adele.cream.annotations.provider.OriginEnum;

import java.util.Set;

public interface RelationProvider {

    String getName();

    Set<String> getProvidedRelations();

    OriginEnum getOrigin(String relation);

    boolean isEnabled(String relation);

    boolean enable(String relation);

    boolean disable(String relation);

    Set<String> getInstances(String relation, boolean includePending);

    boolean deleteInstances(String relation, boolean onlyPending);

    Set<String> getPotentiallyProvidedRelationServices(String relation);

    Set<String> getPotentiallyProvidedRelationServices();

    Set<String> getPotentiallyRequiredServices(String relation);

    Set<String> getPotentiallyRequiredServices();
}
