package fr.liglab.adele.cream.model.introspection;

import java.util.Set;

public interface RelationProvider {

    String getName();

    Set<String> getProvidedRelations();

    boolean isEnabled(String relation);

    boolean enable(String relation);

    boolean disable(String relation);

}
