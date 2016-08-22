package fr.liglab.adele.cream.it.entity.services;

import fr.liglab.adele.cream.annotations.ContextService;

/**
 * Created by aygalinc on 18/08/16.
 */
@ContextService
public interface ContextServiceHeritage extends ContextService1 {

    boolean returnTrue();
}
