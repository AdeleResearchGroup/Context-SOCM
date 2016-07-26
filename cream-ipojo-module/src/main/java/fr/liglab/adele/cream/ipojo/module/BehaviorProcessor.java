package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import java.lang.annotation.Annotation;

/**
 * Created by aygalinc on 15/01/16.
 */
public class BehaviorProcessor extends AbstractBehaviorElementProcessor<Behavior>  {


	public BehaviorProcessor(ClassLoader classReferenceLoader) {
		super(Behavior.class,classReferenceLoader);
	}

	@Override
	public void process(Behavior annotation) {
		buildSubBehaviorElement(annotation);
	}

}
