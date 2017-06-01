package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalExtender(contextServices = ExtensionServiceWithParameters.class)
public class ExtenderWithParameters implements ExtensionServiceWithParameters {

    @ContextEntity.State.Field(service = ExtensionServiceWithParameters.class, state = ExtensionServiceWithParameters.PARAM_1_DIRECTACCESS, directAccess = true, value = "true")
    public Boolean param1;

    /**
     * PARAM 2 : Pull only
     */
    @ContextEntity.State.Field(service = ExtensionServiceWithParameters.class, state = ExtensionServiceWithParameters.PARAM_2_PULL)
    public Boolean param2;
    /**
     * PARAM 3 : APPLY only
     */
    @ContextEntity.State.Field(service = ExtensionServiceWithParameters.class, state = ExtensionServiceWithParameters.PARAM_3_APPLY)
    public Boolean param3;
    /**
     * PARAM 4 : PERIODIC PULL only
     */
    @ContextEntity.State.Field(service = ExtensionServiceWithParameters.class, state = ExtensionServiceWithParameters.PARAM_4_PERIODICPULL)
    public Long param4;
    /**
     * PARAM 5 : PULL + APPLY
     */
    @ContextEntity.State.Field(service = ExtensionServiceWithParameters.class, state = ExtensionServiceWithParameters.PARAM_5_PULLAPPLY)
    public Boolean param5;
    @ContextEntity.State.Pull(service = ExtensionServiceWithParameters.class, state = ExtensionServiceWithParameters.PARAM_2_PULL)
    Supplier<Boolean> param2Supplier = () -> true;
    Boolean param3Apply = true;
    @ContextEntity.State.Apply(service = ExtensionServiceWithParameters.class, state = ExtensionServiceWithParameters.PARAM_3_APPLY)
    Consumer<Boolean> param3Consumer = (Boolean x) -> param3Apply = x;
    @ContextEntity.State.Pull(service = ExtensionServiceWithParameters.class, state = ExtensionServiceWithParameters.PARAM_4_PERIODICPULL, period = 2, unit = TimeUnit.SECONDS)
    Supplier<Long> param4PeriodicSupplier = () -> System.currentTimeMillis();
    Boolean param5Apply = true;

    @ContextEntity.State.Pull(service = ExtensionServiceWithParameters.class, state = ExtensionServiceWithParameters.PARAM_5_PULLAPPLY)
    Supplier<Boolean> param5Supplier = () -> true;

    @ContextEntity.State.Apply(service = ExtensionServiceWithParameters.class, state = ExtensionServiceWithParameters.PARAM_5_PULLAPPLY)
    Consumer<Boolean> param5Consumer = (Boolean x) -> param5Apply = x;
}
