package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.entity.ContextEntity.State;


/**
 * Created by aygalinc on 15/01/16.
 */
public class StateVariableFieldProcessor extends StateProcessor<State.Field> {

    public StateVariableFieldProcessor(ClassLoader classReferenceLoader) {
        super(State.Field.class, classReferenceLoader);
    }

    @Override
    protected String getStateId() {
        return State.id(getAnnotation().service(), getAnnotation().state());
    }

    @Override
    protected void processStateAttributes() {
        setDirectAccess(getAnnotation().directAccess());
        addStateAttribute("field", getAnnotatedField().name, true);
        addStateAttribute("value", getAnnotation().value(), true);
    }

}
