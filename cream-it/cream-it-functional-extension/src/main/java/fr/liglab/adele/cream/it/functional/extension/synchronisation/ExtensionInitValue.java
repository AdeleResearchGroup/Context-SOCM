package fr.liglab.adele.cream.it.functional.extension.synchronisation;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

/**
 * Created by aygalinc on 25/08/16.
 */
public
@ContextService
interface ExtensionInitValue {

    @State
    String PARAM_TO_INIT = "paramToInit";

    boolean returnInitValue();
}
