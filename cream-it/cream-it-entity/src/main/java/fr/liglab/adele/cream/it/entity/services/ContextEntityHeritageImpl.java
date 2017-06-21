package fr.liglab.adele.cream.it.entity.services;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(coreServices = ContextServiceHeritage.class)
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
