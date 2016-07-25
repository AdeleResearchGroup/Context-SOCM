package fr.liglab.adele.cream.it.behavior;


import fr.liglab.adele.cream.annotations.behavior.BehaviorProvider;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@BehaviorProvider(spec = BehaviorSpec1.class)
public class BehviorImpl1WithDirectAccess implements BehaviorSpec1 {

    @ContextEntity.State.Field(service = BehaviorSpec1.class,state = BehaviorSpec1.PARAM_1,directAccess = true)
    public Boolean param1;

    public boolean getterMethod() {
        return param1;
    }

    public void setterMethod(Boolean param1) {
        this.param1 = param1;
    }
}