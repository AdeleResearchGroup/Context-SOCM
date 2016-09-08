package fr.liglab.adele.cream.it.entity.services;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;

@ContextEntity(services = ContextServiceHeritage.class)
public class ContextEntityExtends extends AbstractContextEntity implements ContextServiceHeritage {

    @Override
    public boolean returnFalse() {
        return false;
    }

}
