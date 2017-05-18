package fr.liglab.adele.cream.annotations.internal;

/**
 * Created by aygalinc on 01/06/16.
 */
public enum ReservedCreamValueReference {

    RECONFIGURATION_FREQUENCY("reconfiguration.frequency"),

    RECONFIGURATION_FREQUENCY_ID("reconfiguration.frequency.id"),

    RECONFIGURATION_FREQUENCY_PERIOD("reconfiguration.frequency.period"),

    RECONFIGURATION_FREQUENCY_UNIT("reconfiguration.frequency.unit"),

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
