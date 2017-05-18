package fr.liglab.adele.cream.administration.api;

import java.util.concurrent.TimeUnit;

/**
 * Created by aygalinc on 16/05/17.
 */
public class ImmutableSynchroPeriod {

    private final String period;

    private final String unit;

    public ImmutableSynchroPeriod(String period,String unit) {
        this.period = period;
        this.unit = unit;
    }

    public String getPeriod() {
        return period;
    }

    public String getUnit() {
        return unit;
    }
}
