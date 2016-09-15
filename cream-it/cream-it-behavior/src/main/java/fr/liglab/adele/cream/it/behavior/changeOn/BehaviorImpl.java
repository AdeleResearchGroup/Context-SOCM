package fr.liglab.adele.cream.it.behavior.changeOn;

import fr.liglab.adele.cream.annotations.behavior.BehaviorProvider;

@BehaviorProvider(spec = BehaviorService.class)
public class BehaviorImpl implements BehaviorService{

    boolean state = true;

    @BehaviorProvider.ChangeOn(spec = ServiceOfContext.class,id = ServiceOfContext.STATE_1)
    public void changeOn(boolean bool){
        state = bool;
    }


    @Override
    public boolean getChange() {
        return state;
    }
}
