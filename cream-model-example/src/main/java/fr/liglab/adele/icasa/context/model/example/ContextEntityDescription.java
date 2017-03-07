package fr.liglab.adele.icasa.context.model.example;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;


public
@ContextService
interface ContextEntityDescription extends ContextEntityFirstLevel {

    public static final
    @State
    String HELLO = "hello";

    String hello();

    void setHello(String hello);

    public String externalNotification(String externalEvent);
}
