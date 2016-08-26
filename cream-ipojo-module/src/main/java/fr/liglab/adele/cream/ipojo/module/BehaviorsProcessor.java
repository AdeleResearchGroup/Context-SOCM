package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.behavior.Behaviors;

/**
 * Created by aygalinc on 15/01/16.
 */
public class BehaviorsProcessor extends AbstractBehaviorElementProcessor<Behaviors>  {

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
