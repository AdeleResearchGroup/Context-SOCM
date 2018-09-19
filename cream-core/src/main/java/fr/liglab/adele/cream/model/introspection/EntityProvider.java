package fr.liglab.adele.cream.model.introspection;


import java.util.Set;

public interface EntityProvider {

    String getName();

    Set<String> getProvidedEntities();

    Set<String> getProvidedServices(String entity);

    Set<String> getInstances(String entity);

    boolean isEnabled(String entity);

    boolean enable(String entity);

    boolean disable(String entity);

}
