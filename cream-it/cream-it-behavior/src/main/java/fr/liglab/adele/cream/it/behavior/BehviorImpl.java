package fr.liglab.adele.cream.it.behavior;


import fr.liglab.adele.cream.annotations.behavior.BehaviorProvider;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@BehaviorProvider(spec = BehaviorSpec1.class)
public class BehviorImpl implements BehaviorSpec1 {

    @ContextEntity.State.Field(service = BehaviorSpec1.class,state = BehaviorSpec1.PARAM_1_DIRECTACCESS,directAccess = true,value = "true")
    public Boolean param1;

    @ContextEntity.State.Field(service = BehaviorSpec1.class,state = BehaviorSpec1.PARAM_2_PULL)
    public Boolean param2;

    @ContextEntity.State.Field(service = BehaviorSpec1.class,state = BehaviorSpec1.PARAM_3_APPLY)
    public Boolean param3;

    boolean param3Apply = true;

    @ContextEntity.State.Field(service = BehaviorSpec1.class,state = BehaviorSpec1.PARAM_4_PERIODICPULL)
    public Long param4;

    @ContextEntity.State.Pull(service = BehaviorSpec1.class,state = BehaviorSpec1.PARAM_2_PULL)
    Supplier<Boolean> param2Supplier = ()-> true;

    @ContextEntity.State.Apply(service = BehaviorSpec1.class,state = BehaviorSpec1.PARAM_3_APPLY)
    Consumer<Boolean> param3Consumer = (Boolean x)-> param3Apply = x;

    @ContextEntity.State.Pull(service = BehaviorSpec1.class,state = BehaviorSpec1.PARAM_4_PERIODICPULL,period = 2,unit = TimeUnit.SECONDS)
    Supplier<Long> param4PeriodicSupplier = ()-> System.currentTimeMillis();

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
    public boolean getterMethodParam3ReturnAlwaysNull() {
        return param3;
    }

    @Override
    public boolean getterMethodParam3WithChange() {
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
}