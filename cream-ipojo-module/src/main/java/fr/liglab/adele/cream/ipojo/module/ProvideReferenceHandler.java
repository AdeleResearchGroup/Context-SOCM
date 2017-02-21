package fr.liglab.adele.cream.ipojo.module;

/**
 * Created by aygalinc on 21/02/17.
 */
public enum ProvideReferenceHandler {

    SPECIFICATIONS("specifications"),

    PROVIDES("provides"),

    STRATEGY("strategy"),

    PROPERTY("property"),

    NAME("name"),

    TYPE("type"),

    VALUE("value"),

    MANDATORY("mandatory"),

    IMMUTABLE("immutable");
    private final String myProperty;

    private ProvideReferenceHandler(String propertyName){
        myProperty = propertyName;
    }


    @Override
    public String toString() {
        return myProperty;
    }
    }
