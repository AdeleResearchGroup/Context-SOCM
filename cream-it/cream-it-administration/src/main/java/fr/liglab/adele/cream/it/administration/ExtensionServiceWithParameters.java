package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

@ContextService
public interface ExtensionServiceWithParameters {

    public static boolean PARAM_1_INIT_VALUE = true;

    public static boolean PARAM_2_VALUE = true;

    public
    @State
    static String PARAM_1_DIRECTACCESS = "directAccessParam";

    public
    @State
    static String PARAM_2_PULL = "pullParam";

    public
    @State
    static String PARAM_3_APPLY = "ApplyParam";

    public
    @State
    static String PARAM_4_PERIODICPULL = "PeriodicPullParam";

    public
    @State
    static String PARAM_5_PULLAPPLY = "ApplyPullParam";


}
