package fr.liglab.adele.cream.it.creator;

import fr.liglab.adele.cream.annotations.provider.Creator;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import java.util.Hashtable;
import java.util.Map;

@Component(immediate = true)
@Provides(specifications = ContextServiceProvider.class)
@Instantiate
public class ContextProvider implements ContextServiceProvider{


    @Creator.Field Creator.Entity<ContextEntity1> contextEntity1Creator;

    public void createInstanceOfcontextEntity(Map prop, String id) {
     contextEntity1Creator.create(id,prop);
    }
}
