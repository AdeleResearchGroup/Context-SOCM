package fr.liglab.adele.cream.model.introspection;

import java.util.Set;

public interface EntityProvider {

    public String getName();

    public Set<String> getProvidedEntities();

    public boolean isEnabled(String entity);

    public boolean enable(String entity);

    public boolean disable(String entity);

    public Set<String> getInstances(String entity, boolean includePending);

    public boolean deleteInstances(String entity, boolean onlyPending);

    public Set<String> getPotentiallyProvidedEntityServices(String entity);

    public Set<String> getPotentiallyProvidedEntityServices();

    public Set<String> getPotentiallyRequiredServices(String entity);

    public Set<String> getPotentiallyRequiredServices();
}
