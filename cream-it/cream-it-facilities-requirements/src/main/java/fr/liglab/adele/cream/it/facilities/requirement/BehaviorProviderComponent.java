package fr.liglab.adele.cream.it.facilities.requirement;

import fr.liglab.adele.cream.annotations.behavior.BehaviorProvider;

@BehaviorProvider(contextServices = BehaviorService.class)
public class BehaviorProviderComponent implements BehaviorService {
    @Override
    public boolean getTrue() {
        return true;
    }
}
