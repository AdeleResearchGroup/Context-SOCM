package fr.liglab.adele.cream.it.functional.extension.changeOn;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

@FunctionalExtender(contextServices = ExtensionSpec.class)
public class ExtenderImpl implements ExtensionSpec {

    boolean state = true;

    @FunctionalExtender.ChangeOn(contextService = ServiceOfContext.class,id = ServiceOfContext.STATE_1)
    public void changeOn(boolean bool){
        state = bool;
    }


    @Override
    public boolean getChange() {
        return state;
    }
}
