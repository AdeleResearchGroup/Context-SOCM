package fr.liglab.adele.cream.it.behavior.synchronisation;

import fr.liglab.adele.cream.annotations.behavior.BehaviorProvider;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@BehaviorProvider(spec = BehaviorInitValue.class)
public class BehaviorInitValueImpl implements BehaviorInitValue {

    @ContextEntity.State.Field(service = BehaviorInitValue.class,state = BehaviorInitValue.PARAM_TO_INIT)
    public boolean paramToInit;

    @Override
    public boolean returnInitValue() {
        return paramToInit;
    }
}
