package fr.liglab.adele.cream.it.facilities.requirement;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

@FunctionalExtender(contextServices = BehaviorServiceHeritage.class)
public class BehaviorHeritageProviderComponent implements BehaviorServiceHeritage {

    @Override
    public boolean getTrue() {
        return true;
    }

}
