package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ContextEntity(services = ContextServiceWithParameters.class)
@FunctionalExtension(id = "extensionWithParam", contextServices = ExtensionServiceWithParameters.class, implementation = ExtensionProviderWithParameters.class)
public class EntityWithParameterOnContextServiceAndBehavior implements ContextServiceWithParameters{


    @ContextEntity.State.Field(service = ContextServiceWithParameters.class, state = ContextServiceWithParameters.PARAM_1_DIRECTACCESS, directAccess = true, value = "true")
    public Boolean param1;

    /**
     * PARAM 2 : Pull only
     */
    @ContextEntity.State.Field(service = ContextServiceWithParameters.class, state = ContextServiceWithParameters.PARAM_2_PULL)
    public Boolean param2;
    /**
     * PARAM 3 : APPLY only
     */
    @ContextEntity.State.Field(service = ContextServiceWithParameters.class, state = ContextServiceWithParameters.PARAM_3_APPLY)
    public Boolean param3;
    /**
     * PARAM 4 : PERIODIC PULL only
     */
    @ContextEntity.State.Field(service = ContextServiceWithParameters.class, state = ContextServiceWithParameters.PARAM_4_PERIODICPULL)
    public Long param4;
    /**
     * PARAM 5 : PULL + APPLY
     */
    @ContextEntity.State.Field(service = ContextServiceWithParameters.class, state = ContextServiceWithParameters.PARAM_5_PULLAPPLY)
    public Boolean param5;
    @ContextEntity.State.Pull(service = ContextServiceWithParameters.class, state = ContextServiceWithParameters.PARAM_2_PULL)
    Supplier<Boolean> param2Supplier = () -> true;
    Boolean param3Apply = true;
    @ContextEntity.State.Apply(service = ContextServiceWithParameters.class, state = ContextServiceWithParameters.PARAM_3_APPLY)
    Consumer<Boolean> param3Consumer = (Boolean x) -> param3Apply = x;
    @ContextEntity.State.Pull(service = ContextServiceWithParameters.class, state = ContextServiceWithParameters.PARAM_4_PERIODICPULL, period = 2, unit = TimeUnit.SECONDS)
    Supplier<Long> param4PeriodicSupplier = () -> System.currentTimeMillis();
    Boolean param5Apply = true;

    @ContextEntity.State.Pull(service = ContextServiceWithParameters.class, state = ContextServiceWithParameters.PARAM_5_PULLAPPLY)
    Supplier<Boolean> param5Supplier = () -> true;

    @ContextEntity.State.Apply(service = ContextServiceWithParameters.class, state = ContextServiceWithParameters.PARAM_5_PULLAPPLY)
    Consumer<Boolean> param5Consumer = (Boolean x) -> param5Apply = x;


    /**
     * SERVICE METHOD
     */
    @Override
    public boolean getterMethodParam1() {
        return param1;
    }

    @Override
    public void setterMethodParam1(Boolean param) {
        this.param1 = param;
    }

    @Override
    public boolean getterMethodParam2() {
        return param2;
    }

    @Override
    public void setterMethodParam2(Boolean param) {
        param2 = param;
    }

    @Override
    public Boolean getterMethodParam3ReturnAlwaysNull() {
        return param3;
    }

    @Override
    public Boolean getterMethodParam3WithChange() {
        return param3Apply;
    }

    @Override
    public void setterMethodParam3(Boolean param) {
        param3 = param;
    }

    @Override
    public long getterMethodParam4() {
        return param4;
    }

    @Override
    public void setterMethodParam4(long param) {
        param4 = param;
    }

    @Override
    public Boolean getterMethodParam5() {
        return param5;
    }

    @Override
    public void setterMethodParam5(boolean param1) {
        param5 = param1;
    }

    @Override
    public Boolean getterMethodParam5WithChange() {
        return param5Apply;
    }
}
