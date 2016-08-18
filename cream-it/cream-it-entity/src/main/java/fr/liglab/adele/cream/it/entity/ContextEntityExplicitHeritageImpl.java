package fr.liglab.adele.cream.it.entity;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ContextServiceHeritage.class)
public class ContextEntityExplicitHeritageImpl implements ContextServiceHeritage,ContextService1 {

    @Override
    public boolean returnFalse() {
        return false;
    }

    @Override
    public boolean returnTrue() {
        return true;
    }
}
