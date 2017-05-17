package fr.liglab.adele.cream.annotations.internal;

/**
 * Created by aygalinc on 01/06/16.
 */
public enum ReservedCreamValueReference {

    NOT_VALUED_STATES("$NOT_VALUED$");


    private final String property;

    ReservedCreamValueReference(String property) {
        this.property = property;
    }


    @Override
    public String toString() {
        return property;
    }
}
