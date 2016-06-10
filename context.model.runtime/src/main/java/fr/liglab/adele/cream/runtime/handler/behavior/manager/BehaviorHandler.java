package fr.liglab.adele.cream.runtime.handler.behavior.manager;

import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.utils.SuccessorStrategy;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Handler(name = BehaviorReference.DEFAULT_BEHAVIOR_TYPE, namespace = BehaviorReference.BEHAVIOR_NAMESPACE)
public class BehaviorHandler extends PrimitiveHandler implements InstanceStateListener,InvocationHandler {

    private final Map<String,RequiredBehavior> myRequiredBehaviorById = new ConcurrentHashMap<>();

    @Override
    public  void configure(Element metadata, Dictionary configuration) throws ConfigurationException {

        getInstanceManager().addInstanceStateListener(this);

        Element[] behaviorElements = metadata.getElements(BehaviorReference.DEFAULT_BEHAVIOR_TYPE,BehaviorReference.BEHAVIOR_NAMESPACE);

        for (Element element:behaviorElements){
            myRequiredBehaviorById.put(element.getAttribute(BehaviorReference.ID_ATTR_NAME),
                    new RequiredBehavior( element.getAttribute(BehaviorReference.SPEC_ATTR_NAME),
                            element.getAttribute(BehaviorReference.IMPLEM_ATTR_NAME))
            );
        }

    }

    @Override
    public  void stop() {

    }

    @Override
    public  void start() {

    }

    /**
     * Issue : behavior must be deactivate before instance become Invalid ...
     */
    @Override
    public  void stateChanged(ComponentInstance instance, int newState) {
        if (newState == ComponentInstance.VALID){
            for (Map.Entry<String,RequiredBehavior> behavior: myRequiredBehaviorById.entrySet()){
                behavior.getValue().tryStartBehavior();
            }
        }

        if (newState == ComponentInstance.INVALID){
            for (Map.Entry<String,RequiredBehavior> behavior: myRequiredBehaviorById.entrySet()){
                behavior.getValue().tryInvalid();
            }
        }
    }

    @Bind(id = "behaviorF",specification = Factory.class,optional = true,proxy = false,aggregate = true,filter = "("+BehaviorReference.BEHAVIOR_TYPE_PROPERTY+"="+BehaviorReference.BEHAVIOR_TYPE+")")
    public void bindBehaviorFactory(Factory behaviorFactory, Map prop){
        for (Map.Entry<String,RequiredBehavior> entry : myRequiredBehaviorById.entrySet()){
            if (match(entry.getValue(),prop)){
                entry.getValue().setFactory(behaviorFactory);
                entry.getValue().addManager();
                if (getInstanceManager().getState() == ComponentInstance.VALID){
                    entry.getValue().tryStartBehavior();
                }
            }
        }
    }

    @Unbind(id = "behaviorF")
    public void unbindBehaviorFactory(Factory behaviorFactory,Map prop){
        for (Map.Entry<String,RequiredBehavior> entry : myRequiredBehaviorById.entrySet()){
            if (match(entry.getValue(),prop)){
                entry.getValue().unRef();
            }
        }
    }


    private List<RequiredBehavior> getBehavior(Element metadata){
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
    }


    protected boolean match(RequiredBehavior req, Map prop) {
        String spec = (String) prop.get(BehaviorReference.SPEC_ATTR_NAME);
        String impl = (String) prop.get(BehaviorReference.IMPLEM_ATTR_NAME);
        return    req.getSpecName().equalsIgnoreCase(spec)  && req.getImplName().equalsIgnoreCase(impl);
    }

    @Override
    public HandlerDescription getDescription() {
        return new BehaviorHandlerDescription();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        for (Map.Entry<String,RequiredBehavior> behaviorEntry : myRequiredBehaviorById.entrySet()){
            Object returnObj = behaviorEntry.getValue().invoke(proxy,method,args);
            if (SuccessorStrategy.NO_FOUND_CODE.equals(returnObj)){
                continue;
            }
            return returnObj;
        }
        return SuccessorStrategy.NO_FOUND_CODE;
    }

    public class BehaviorHandlerDescription extends HandlerDescription {

        public BehaviorHandlerDescription(){
            super(BehaviorHandler.this);
        }

        @Override
        public Element getHandlerInfo() {
            Element element = super.getHandlerInfo();
            for (Map.Entry<String,RequiredBehavior> entry : myRequiredBehaviorById.entrySet()){
                entry.getValue().getBehaviorDescription(element);
            }
            return element;
        }
    }

}
