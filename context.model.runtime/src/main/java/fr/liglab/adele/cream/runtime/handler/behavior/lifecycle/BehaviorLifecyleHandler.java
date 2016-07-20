package fr.liglab.adele.cream.runtime.handler.behavior.lifecycle;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.metadata.Element;

import java.util.Dictionary;

@Handler(name = HandlerReference.BEHAVIOR_LIFECYCLE_HANDLER, namespace = HandlerReference.NAMESPACE)
public class BehaviorLifecyleHandler extends PrimitiveHandler {

    private final Object m_lock = new Object();

    @Override
    public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {

    }

    @Override
    public synchronized void stop() {

    }

    @Override
    public synchronized void start() {
        setValidity(false);
    }

    public void stopBehavior() {
        synchronized (m_lock) {
            if (this.getValidity()) {
                setValidity(false);
            }
        }
    }

    public void startBehavior(){
        synchronized (m_lock) {
            if (!this.getValidity()) {
                setValidity(true);
            }
        }
    }
}
