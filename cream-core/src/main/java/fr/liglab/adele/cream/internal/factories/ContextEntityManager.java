package fr.liglab.adele.cream.internal.factories;

import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.HandlerFactory;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.util.Log;
import org.apache.felix.ipojo.util.Logger;
import org.apache.felix.ipojo.util.Tracker;
import org.apache.felix.ipojo.util.TrackerCustomizer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

/**
 * Created by aygalinc on 31/05/16.
 */
public class ContextEntityManager extends InstanceManager implements TrackerCustomizer {

    private final List<RequiredBehavior> myRequiredBehavior = new ArrayList<>();

    /**
     * The tracker used to track required handler factories.
     * Immutable once set.
     */
    protected Tracker m_tracker;

    /**
     * Creates a new Component Manager.
     * The instance is not initialized.
     *
     * @param factory  the factory managing the instance manager
     * @param context  the bundle context to give to the instance
     * @param handlers handler object array
     */
    public ContextEntityManager(ComponentFactory factory, BundleContext context, HandlerManager[] handlers) {
        super(factory, context, handlers);

    }

    private List<RequiredBehavior> getBehavior (Element metadata, Dictionary configuration){
        List<RequiredBehavior> behaviors = new ArrayList<>();
        Element[] behaviorsElements = metadata.getElements("behaviors");
        if (behaviorsElements == null) {
            return behaviors;
        }

        for (Element behavior : behaviorsElements){
            String behaviorSpec = behavior.getAttribute("spec");
            String behaviorImplem = behavior.getAttribute("implem");
            if ((behaviorSpec == null) || (behaviorImplem == null)){
                getLogger().log(Log.WARNING, "behavior spec or implem is null");
                continue;
            }
            RequiredBehavior requiredBehavior = new RequiredBehavior(behaviorSpec,behaviorImplem);
            behaviors.add(requiredBehavior);
        }

        return behaviors;
    }

    @Override
    public void start() {
        super.start();
        synchronized (this){
            if (m_state == VALID){
                if (m_tracker == null) {
                    if (myRequiredBehavior.size() != 0) {
                        try {
                            String filter = "(&(" + BehaviorReference.BEHAVIOR_TYPE_PROPERTY + "=" + BehaviorReference.BEHAVIOR_TYPE + ")" + "(factory.state=1)" + ")";
                            m_tracker = new Tracker(getContext(), getContext().createFilter(filter), this);
                            m_tracker.open();
                        } catch (InvalidSyntaxException e) {
                            getLogger().log(Logger.ERROR, "A factory filter is not valid: " + e.getMessage()); //Holding the lock should not be an issue here.
                            stop();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void stop() {
        synchronized (this){
            if (m_state == VALID){
                if (m_tracker != null) {
                    m_tracker.close();
                    m_tracker = null;
                }
            }
        }
        super.stop();
    }


    @Override
    public synchronized boolean addingService(ServiceReference reference) {
        for (int i = 0; i < myRequiredBehavior.size(); i++) {
            RequiredBehavior req =  myRequiredBehavior.get(i);
            if (req.getReference() == null && match(req, reference)) {
                req.setReference(reference);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void addedService(ServiceReference reference) {
        if (m_state == VALID) {

        }
    }

    @Override
    public synchronized void modifiedService(ServiceReference reference, Object service) {

    }

    @Override
    //TODO : start a dispose sequence in the behavior
    public synchronized void removedService(ServiceReference reference, Object service) {
        // Look for the implied reference and invalid the handler identifier
        for (RequiredBehavior req : myRequiredBehavior) {
            if (reference.equals(req.getReference())) {
                req.unRef(); // This method will unget the service.
                return; // The factory can be used only once.
            }
        }
    }



    protected boolean match(RequiredBehavior req, ServiceReference<?> ref) {
        String spec = (String) ref.getProperty(BehaviorReference.SPEC_ATTR_NAME);
        String impl = (String) ref.getProperty(BehaviorReference.IMPLEM_ATTR_NAME);

        return spec.equalsIgnoreCase(req.getSpecName()) && impl.equalsIgnoreCase(req.getImplName());
    }

    /**
     * Structure storing required handlers.
     * Access to this class must mostly be with the lock on the factory.
     * (except to access final fields)
     */
    protected class RequiredBehavior {
        /**
         * The factory to create this behvior.
         */
        private BehaviorFactory myFactory;

        /**
         * The behavior name.
         */
        private final String myName;

        /**
         * The behavior impl name.
         */
        private final String myBehaviorNameImpl;

        /**
         * The Service Reference of the handler factory.
         */
        private ServiceReference<? extends BehaviorFactory> m_reference;

        /**
         * Crates a Required Handler.
         * @param name the handler name.
         * @param behaviorNameImpl the handler namespace.
         */
        public RequiredBehavior(String name, String behaviorNameImpl) {
            myName = name;
            myBehaviorNameImpl = behaviorNameImpl;
        }

        /**
         * Equals method.
         * Two handlers are equals if they have same name and namespace or they share the same service reference.
         * @param object the object to compare to the current object.
         * @return <code>true</code> if the two compared object are equals
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object object) {
            if (object instanceof RequiredBehavior) {
                RequiredBehavior req = (RequiredBehavior) object;
                if (myBehaviorNameImpl == null) {
                    return req.myName.equalsIgnoreCase(m_name) && req.myBehaviorNameImpl == null;
                } else {
                    return req.myName.equalsIgnoreCase(m_name) && myBehaviorNameImpl.equalsIgnoreCase(req.myBehaviorNameImpl);
                }
            } else {
                return false;
            }

        }

        /**
         * Hashcode method.
         * This method delegates to the {@link Object#hashCode()}.
         * @return the object hashcode.
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return super.hashCode();
        }

        /**
         * Gets the factory object used for this handler.
         * The object is get when used for the first time.
         * This method is called with the lock avoiding concurrent modification and on a valid factory.
         * @return the factory object.
         */
        public BehaviorFactory getFactory() {
            if (m_reference == null) {
                return null;
            }
            if (myFactory == null) {
                myFactory = getContext().getService(getReference());
            }
            return myFactory;
        }

        /**
         * Gets the handler qualified name (<code>namespace:name</code>).
         * @return the handler full name
         */
        public String getFullName() {
            if (myBehaviorNameImpl == null) {
                return HandlerFactory.IPOJO_NAMESPACE + ":" + m_name;
            } else {
                return myBehaviorNameImpl + ":" + m_name;
            }
        }

        public String getSpecName() {
            return m_name;
        }

        public String getImplName() {
            return myBehaviorNameImpl;
        }

        public ServiceReference<? extends BehaviorFactory> getReference() {
            return m_reference;
        }

        /**
         * Releases the reference of the used factory.
         * This method is called with the lock on the current factory.
         */
        public void unRef() {
            if (m_reference != null) {
                myFactory = null;
                m_reference = null;
            }
        }

        /**
         * Sets the service reference. If the new service reference is <code>null</code>, it ungets the used factory (if already get).
         * This method is called with the lock on the current factory.
         * @param ref the new service reference.
         */
        public void setReference(ServiceReference<? extends BehaviorFactory> ref) {
            m_reference = ref;
        }
    }
}
