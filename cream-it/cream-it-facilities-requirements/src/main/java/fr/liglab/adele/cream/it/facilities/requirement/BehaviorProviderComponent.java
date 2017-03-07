package fr.liglab.adele.cream.it.facilities.requirement;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

@FunctionalExtender(contextServices = BehaviorService.class)
public class BehaviorProviderComponent implements BehaviorService {
    @Override
    public boolean getTrue() {
        return true;
    }
}
