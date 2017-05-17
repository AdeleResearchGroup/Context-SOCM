package fr.liglab.adele.cream.administration.api;

import java.util.concurrent.TimeUnit;

/**
 * Created by aygalinc on 12/05/17.
 */
public class ImmutableContextState {

    private final String id;

    private final String value;

    private final ImmutableSynchroPeriod synchroPeriod;

    public ImmutableContextState(String id, String value, String synchroPeriod, String synchroUnit) {
        this.id = id;
        this.value = value;
        if (synchroPeriod == null || synchroUnit == null){
            this.synchroPeriod = null;
        }else{
            this.synchroPeriod = new ImmutableSynchroPeriod(synchroPeriod,synchroUnit);
        }
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public ImmutableSynchroPeriod getSynchroPeriod() {
        return synchroPeriod;
    }

}
