package fr.liglab.adele.cream.it.administration;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.List;

@ContextEntity
public class EntityWithDependencies {

    @Requires(optional = true,specification = ContextServiceWithParameters.class)
    List<ContextServiceWithParameters> contextServiceWithParameterss;

    @Requires(optional = true,specification = ContextServiceWithoutParameters.class)
    List<ContextServiceWithoutParameters> contextServiceWithoutParameterss;

    @Requires(optional = true,specification = RegularIPOJOService.class)
    List<RegularIPOJOService> ipojoServices;
}
