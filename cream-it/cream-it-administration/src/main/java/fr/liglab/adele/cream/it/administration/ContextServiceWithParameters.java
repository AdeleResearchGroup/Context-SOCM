package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.State;

@fr.liglab.adele.cream.annotations.ContextService
public interface ContextServiceWithParameters {

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

    boolean getterMethodParam1();

    void setterMethodParam1(Boolean param1);

    boolean getterMethodParam2();

    void setterMethodParam2(Boolean param1);

    Boolean getterMethodParam3ReturnAlwaysNull();

    Boolean getterMethodParam3WithChange();

    void setterMethodParam3(Boolean param1);

    long getterMethodParam4();

    void setterMethodParam4(long param1);

    Boolean getterMethodParam5();

    void setterMethodParam5(boolean param1);

    Boolean getterMethodParam5WithChange();
}
