package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.behavior.Behaviors;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

/**
 * Created by aygalinc on 15/01/16.
 */
public class BehaviorsProcessor extends AbstractBehaviorElementProcessor<Behaviors>  {

	protected static final String BEHAVIOR_ELEMENT = HandlerReference.NAMESPACE +":"+ HandlerReference.BEHAVIOR_MANAGER_HANDLER;

	public BehaviorsProcessor(ClassLoader classReferenceLoader) {
		super(Behaviors.class,classReferenceLoader);
	}

	@Override
	public void process(Behaviors annotation) {
		for (Behavior behavior : annotation.value()){
			buildSubBehaviorElement(behavior);
		}
	}

}
