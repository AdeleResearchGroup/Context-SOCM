package fr.liglab.adele.cream.runtime.handler.behavior.lifecycle;

/**
 * Created by aygalinc on 01/09/16.
 */
public interface BehaviorStateListener {

    public void behaviorStateChange(int state,String behaviorId);
}
