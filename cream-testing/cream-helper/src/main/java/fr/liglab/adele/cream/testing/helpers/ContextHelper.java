package fr.liglab.adele.cream.testing.helpers;

import fr.liglab.adele.cream.testing.helpers.ContextEntityHelper;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

/**
 * Created by aygalinc on 25/07/16.
 */
public class ContextHelper{

    private final OSGiHelper osGiHelper;
    private final IPOJOHelper ipojoHelper;

    private final ContextEntityHelper contextEntityHelper;

    private final BehaviorHelper behaviorHelper;

    public ContextHelper(OSGiHelper osGiHelper, IPOJOHelper ipojoHelper) {
        this.osGiHelper = osGiHelper;
        this.ipojoHelper = ipojoHelper;
        behaviorHelper = new BehaviorHelper(osGiHelper,ipojoHelper);
        contextEntityHelper = new ContextEntityHelper(osGiHelper,ipojoHelper);
    }

    public ContextEntityHelper getContextEntityHelper(){
        return contextEntityHelper;
    }

    public BehaviorHelper getBehaviorHelper(){
        return behaviorHelper;
    }



}
