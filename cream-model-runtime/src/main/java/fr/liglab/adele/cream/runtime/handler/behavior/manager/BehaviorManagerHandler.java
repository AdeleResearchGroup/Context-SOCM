package fr.liglab.adele.cream.runtime.handler.behavior.manager;

import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.utils.SuccessorStrategy;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Handler(name = HandlerReference.BEHAVIOR_MANAGER_HANDLER, namespace = HandlerReference.NAMESPACE)
public class BehaviorManagerHandler extends PrimitiveHandler implements InvocationHandler,ContextListener {

    private final Map<String,RequiredBehavior> myRequiredBehaviorById = new ConcurrentHashMap<>();

    private final Set<String> stateVariable = new ConcurrentSkipListSet<>();

    private final Object lockValidity = new Object();

    @Override
    public  void configure(Element metadata, Dictionary configuration) throws ConfigurationException {

        Element[] behaviorElements = metadata.getElements(HandlerReference.BEHAVIOR_MANAGER_HANDLER,HandlerReference.NAMESPACE);

        if (behaviorElements == null) {
            throw new ConfigurationException("Behavior Elements are null ");
        }

        for (Element element:behaviorElements){
            Element[] behaviorIndividualElements = element.getElements(BehaviorReference.BEHAVIOR_INDIVIDUAL_ELEMENT_NAME,"");

            if (behaviorIndividualElements == null){
                throw new ConfigurationException("Behavior Individual Element is null ");
            }

            for (Element individualBehaviorElement:behaviorIndividualElements) {
                RequiredBehavior requiredBehavior = new RequiredBehavior(individualBehaviorElement.getAttribute(BehaviorReference.SPECIFICATION_ATTRIBUTE_NAME), individualBehaviorElement.getAttribute(BehaviorReference.IMPLEMEMENTATION_ATTRIBUTE_NAME), configuration);
                myRequiredBehaviorById.put(individualBehaviorElement.getAttribute(BehaviorReference.ID_ATTRIBUTE_NAME),requiredBehavior);

                String fieldAttribute = individualBehaviorElement.getAttribute(BehaviorReference.FIELD_ATTRIBUTE_NAME);
                FieldMetadata fieldMetadata = null;
                if (fieldAttribute != null){
                    fieldMetadata = getPojoMetadata().getField(fieldAttribute);
                }
                if (fieldMetadata != null){
                    getInstanceManager().register(fieldMetadata,requiredBehavior.getBehaviorInterceptor());
                }
            }
        }
        setValidity(false);


    }

    @Override
    public  synchronized void stop() {
        for (Map.Entry<String,RequiredBehavior> entry : myRequiredBehaviorById.entrySet()){
            entry.getValue().tryDispose();
        }
        stateVariable.clear();
    }

    @Override
    public void start() {
        //Do nothing
    }


    private ProvidedServiceHandler getProvideServiceHandler(){
        return (ProvidedServiceHandler) getHandler(HandlerFactory.IPOJO_NAMESPACE + ":provides");
    }

    @Validate
    public void validate(){
        checkValidity();
    }

    @Invalidate
    public void invalidate(){
        stateVariable.clear();
    }

    /**
     * Issue : behavior must be deactivate before instance become Invalid ...
     */
    @Override
    public  void stateChanged(int newState) {
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

    @Bind(id = "behaviorF",specification = Factory.class,optional = false,proxy = false,aggregate = true,filter = "("+BehaviorReference.BEHAVIOR_FACTORY_TYPE_PROPERTY +"="+BehaviorReference.BEHAVIOR_FACTORY_TYPE_PROPERTY_VALUE +")")
    public void bindBehaviorFactory(Factory behaviorFactory, Map prop){
        for (Map.Entry<String,RequiredBehavior> entry : myRequiredBehaviorById.entrySet()){
            if (match(entry.getValue(),prop)){
                entry.getValue().setFactory(behaviorFactory);
                entry.getValue().addManager();
                entry.getValue().registerBehaviorListener(this);
                checkValidity();
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
        checkValidity();
    }

    private void checkValidity(){
        for (Map.Entry<String,RequiredBehavior> entry : myRequiredBehaviorById.entrySet()){
            if (entry.getValue().isOperationnal()){
                continue;
            }
            synchronized (lockValidity) {
                setValidity(false);
                return;
            }
        }
        synchronized (lockValidity) {
            if (isOperationnal()) {
                for (Map.Entry<String, RequiredBehavior> entry : myRequiredBehaviorById.entrySet()) {
                    entry.getValue().tryStartBehavior();
                }
            }
            setValidity(true);
        }
    }

    public boolean isOperationnal(){
        for (org.apache.felix.ipojo.Handler handler : this.getInstanceManager().getRegisteredHandlers()){
            HandlerFactory fact = (HandlerFactory) handler.getHandlerManager().getFactory();
            if (fact.getHandlerName().equals(HandlerReference.NAMESPACE+":"+HandlerReference.BEHAVIOR_MANAGER_HANDLER)) {
                continue;
            }
            if (!handler.getValidity()){
                return false;
            }
        }
        return true;
    }

    protected boolean match(RequiredBehavior req, Map prop) {
        String spec = (String) prop.get(BehaviorReference.SPECIFICATION_ATTRIBUTE_NAME);
        String impl = (String) prop.get(BehaviorReference.IMPLEMEMENTATION_ATTRIBUTE_NAME);
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

    /**
     * Context Listener Implem
     */
    @Override
    public synchronized void update(ContextSource contextSource, String s, Object o) {

        ProvidedServiceHandler providerHandler = getProvideServiceHandler();
        if (providerHandler == null){
            return;
        }


        Hashtable<String,Object> property = new Hashtable<>();
        property.put(s, o);
        if (o == null){
            stateVariable.remove(s);
            getProvideServiceHandler().removeProperties(property);
            return;
        }

        if (stateVariable.contains(s)){
            providerHandler.reconfigure(property);
        }else {
            stateVariable.add(s);
            providerHandler.addProperties(property);
        }

    }

    public class BehaviorHandlerDescription extends HandlerDescription {

        public BehaviorHandlerDescription(){
            super(BehaviorManagerHandler.this);
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
