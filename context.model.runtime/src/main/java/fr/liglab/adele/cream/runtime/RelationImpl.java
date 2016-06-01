package fr.liglab.adele.cream.runtime;

import fr.liglab.adele.cream.model.Relation;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;

@Component
@Provides
public class RelationImpl implements Relation {

    @Property(name = "relation.source.id",mandatory = true)
    public String source;

    @Property(name = "relation.target.id",mandatory = true)
    public String target;

    @Property(name = "relation.id",mandatory = true)
    public String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public String getTarget() {
		return target;
	}
    
 
}
