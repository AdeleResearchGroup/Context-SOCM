package fr.liglab.adele.cream.model.introspection;

import fr.liglab.adele.cream.annotations.provider.OriginEnum;

import java.util.Set;

public interface EntityProvider {

    String getName();

    Set<String> getProvidedEntities();

    OriginEnum getOrigin(String entity);

    boolean isEnabled(String entity);

    boolean enable(String entity);

    boolean disable(String entity);

    Set<String> getInstances(String entity, boolean includePending);

    boolean deleteInstances(String entity, boolean onlyPending);

    Set<String> getPotentiallyProvidedEntityServices(String entity);

    Set<String> getPotentiallyProvidedEntityServices();

    Set<String> getPotentiallyRequiredServices(String entity);

    Set<String> getPotentiallyRequiredServices();
}
