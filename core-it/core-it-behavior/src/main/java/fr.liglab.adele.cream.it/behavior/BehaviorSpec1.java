package fr.liglab.adele.cream.it.behavior;


import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

@ContextService
public interface BehaviorSpec1 {

    public @State static String PARAM_1 = "param1";

    boolean getterMethod();

    void setterMethod(Boolean param1);

}
