package fr.liglab.adele.cream.annotations.internal;

/**
 * Created by aygalinc on 01/06/16.
 */
public enum  BehaviorReference {

    BEHAVIOR_INDIVIDUAL_ELEMENT_NAME("behavior"),

    SPECIFICATION_ATTRIBUTE_NAME("behavior.spec"),

    IMPLEMEMENTATION_ATTRIBUTE_NAME("implementation"),

    FIELD_ATTRIBUTE_NAME ("field"),

    ID_ATTRIBUTE_NAME ("id"),

    BEHAVIOR_MANDATORY_ATTRIBUTE_NAME("mandatory"),

    BEHAVIOR_ID_CONFIG("behavior.id");

    private final String property;

    /**
     * Constante used in filter
     */
    public final static String BEHAVIOR_FACTORY_TYPE_PROPERTY ="behavior.factory.property";

    public final static String BEHAVIOR_FACTORY_TYPE_PROPERTY_VALUE="behavior.factory";

    BehaviorReference(String property){
        this.property = property;
    }


    @Override
    public String toString() {
        return property;
    }
}
