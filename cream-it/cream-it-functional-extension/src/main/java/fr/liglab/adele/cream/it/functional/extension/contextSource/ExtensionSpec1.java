package fr.liglab.adele.cream.it.functional.extension.contextSource;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

/**
 * Created by aygalinc on 26/09/16.
 */
public
@ContextService
interface ExtensionSpec1 {

    @State
    String EXTENSION_STATE = "state";

    void setValue(String filterValue);
}
