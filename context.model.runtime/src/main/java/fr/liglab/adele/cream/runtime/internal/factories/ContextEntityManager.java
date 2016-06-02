package fr.liglab.adele.cream.runtime.internal.factories;

import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import org.apache.felix.ipojo.*;
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
public class ContextEntityManager extends InstanceManager /**implements TrackerCustomizer**/ {

  //  private final List<RequiredBehavior> myRequiredBehavior = new ArrayList<>();

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

    public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
        super.configure(metadata,configuration);

   //     myRequiredBehavior.addAll(getBehavior(metadata));
    }



    @Override
    public void start() {
        super.start();

     /**   synchronized (this){
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
        }**/
    }

    @Override
    public void stop() {
       /** synchronized (this){
            if (m_state == VALID){
                if (m_tracker != null) {
                    m_tracker.close();
                    m_tracker = null;
                }
            }
        }**/
        super.stop();
    }

    /**  private List<RequiredBehavior> getBehavior (Element metadata){
     List<RequiredBehavior> behaviors = new ArrayList<>();
     Element[] behaviorsElements = metadata.getElements(BehaviorReference.DEFAULT_BEHAVIOR_TYPE,BehaviorReference.BEHAVIOR_NAMESPACE);
     if (behaviorsElements == null) {
     return behaviors;
     }

     for (Element behavior : behaviorsElements){
     String behaviorSpec = behavior.getAttribute(BehaviorReference.SPEC_ATTR_NAME);
     String behaviorImplem = behavior.getAttribute(BehaviorReference.IMPLEM_ATTR_NAME);
     if ((behaviorSpec == null) || (behaviorImplem == null)){
     getLogger().log(Log.WARNING, "behavior spec or implem is null");
     continue;
     }
     RequiredBehavior requiredBehavior = new RequiredBehavior(behaviorSpec,behaviorImplem);
     behaviors.add(requiredBehavior);
     }

     return behaviors;
     }**/

  /**  @Override
    public synchronized boolean addingService(ServiceReference reference) {
      System.out.println("Bhv detected ");
        for (int i = 0; i < myRequiredBehavior.size(); i++) {
            RequiredBehavior req =  myRequiredBehavior.get(i);
            System.out.println("Bhv check " + req.getSpecName() + " impl " + req.getImplName());
            if (req.getReference() == null && match(req, reference)) {
                System.out.println(" Check ok");
                req.setReference(reference);
                return true;
            }
        }
        System.out.println(" Check ko");
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
        System.out.println(" Service spec " + spec + " against  " + req.getSpecName());
        System.out.println(" Service impl " + impl + " against  " + req.getImplName());
        return    req.getSpecName().equalsIgnoreCase(spec)  && req.getImplName().equalsIgnoreCase(impl);
    }


    protected class RequiredBehavior {

        private BehaviorFactory myFactory;


        private final String myName;


        private final String myBehaviorNameImpl;


        private ServiceReference<? extends BehaviorFactory> m_reference;


        public RequiredBehavior(String name, String behaviorNameImpl) {
            myName = name;
            myBehaviorNameImpl = behaviorNameImpl;
        }

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


        public int hashCode() {
            return super.hashCode();
        }


        public BehaviorFactory getFactory() {
            if (m_reference == null) {
                return null;
            }
            if (myFactory == null) {
                myFactory = getContext().getService(getReference());
            }
            return myFactory;
        }



        public String getSpecName() {
            return myName;
        }

        public String getImplName() {
            return myBehaviorNameImpl;
        }

        public ServiceReference<? extends BehaviorFactory> getReference() {
            return m_reference;
        }


        public void unRef() {
            if (m_reference != null) {
                myFactory = null;
                m_reference = null;
            }
        }

        public void setReference(ServiceReference<? extends BehaviorFactory> ref) {
            m_reference = ref;
        }
    }**/
}
