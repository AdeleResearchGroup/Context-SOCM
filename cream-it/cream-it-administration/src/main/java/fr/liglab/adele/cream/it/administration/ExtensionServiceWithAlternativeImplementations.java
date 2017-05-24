package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.ContextService;

@ContextService
public interface ExtensionServiceWithAlternativeImplementations {

    public String getExtenderClassName();
}
