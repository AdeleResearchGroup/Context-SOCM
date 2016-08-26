package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.entity.ContextEntity.State;

/**
 * Created by aygalinc on 15/01/16.
 */
public class PushMethodProcessor extends StateProcessor<State.Push>  {

	public PushMethodProcessor(ClassLoader classReferenceLoader) {
		super(State.Push.class,classReferenceLoader);
	}

	@Override
	protected String getStateId() {
		return State.id(getAnnotation().service(), getAnnotation().state());
	}

	@Override
	protected void processStateAttributes() {
		addStateAttribute("push",getAnnotatedMethod().name,false);
	}

}
