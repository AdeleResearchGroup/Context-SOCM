package fr.liglab.adele.cream.testing.helpers;

import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

/**
 * Created by aygalinc on 25/07/16.
 */
public class ContextHelper {

    private final OSGiHelper osGiHelper;
    private final IPOJOHelper ipojoHelper;

    private final ContextEntityHelper contextEntityHelper;

    private final FunctionalExtensionHelper functionalExtensionHelper;

    public ContextHelper(OSGiHelper osGiHelper, IPOJOHelper ipojoHelper) {
        this.osGiHelper = osGiHelper;
        this.ipojoHelper = ipojoHelper;
        functionalExtensionHelper = new FunctionalExtensionHelper(ipojoHelper);
        contextEntityHelper = new ContextEntityHelper(osGiHelper, ipojoHelper);
    }

    public ContextEntityHelper getContextEntityHelper() {
        return contextEntityHelper;
    }

    public FunctionalExtensionHelper getFunctionalExtensionHelper() {
        return functionalExtensionHelper;
    }


}
