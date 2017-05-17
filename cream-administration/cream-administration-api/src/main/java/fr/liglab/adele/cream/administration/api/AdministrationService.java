package fr.liglab.adele.cream.administration.api;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by aygalinc on 12/05/17.
 */
public interface AdministrationService {

    Set<ImmutableContextEntity> getContextEntities();

    ImmutableContextEntity getContextEntity(String id);

    void reconfigureContextEntityFrequency(String contextEntityId, String contextStateId, int frequency, TimeUnit unit);

    void reconfigureContextEntityComposition(String contextEntityId,String functionnalExtensionId,String functionnalExtensionImplementation);
}
