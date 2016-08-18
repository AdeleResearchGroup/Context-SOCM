package fr.liglab.adele.cream.it.entity;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ContextServiceHeritage.class)
public class ContextEntityHeritageImpl implements ContextServiceHeritage {

    @Override
    public boolean returnFalse() {
        return false;
    }

    @Override
    public boolean returnTrue() {
        return true;
    }
}
