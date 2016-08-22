package fr.liglab.adele.cream.it.entity.services;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ContextService1.class)
public class ContextEntity1 implements ContextService1 {

    @Override
    public boolean returnFalse() {
        return false;
    }
}
