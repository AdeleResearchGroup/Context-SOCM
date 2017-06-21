package fr.liglab.adele.cream.it.entity.services;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(coreServices = ContextServiceHeritage.class)
public class ContextEntityExplicitHeritageImpl implements ContextServiceHeritage, ContextService1 {

    @Override
    public boolean returnFalse() {
        return false;
    }

    @Override
    public boolean returnTrue() {
        return true;
    }
}
