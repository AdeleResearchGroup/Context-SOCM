package fr.liglab.adele.cream.runtime.handler.behavior.manager;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.internal.BehaviorReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.runtime.handler.behavior.lifecycle.BehaviorStateListener;
import fr.liglab.adele.cream.utils.SuccessorStrategy;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedService;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.ParseUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Handler(name = HandlerReference.BEHAVIOR_MANAGER_HANDLER, namespace = HandlerReference.NAMESPACE)
public class BehaviorManagerHandler extends PrimitiveHandler implements InvocationHandler,ContextListener,BehaviorStateListener {

    private static final String CONTEXT_ENTITY_CONTROLLER_FIELD_NAME = " context.entity.controller.";

    private final Map<String,RequiredBehavior> myRequiredBehaviorById = new ConcurrentHashMap<>();

    private final Set<String> stateVariable = new ConcurrentSkipListSet<>();

    @Override
    public  void configure(Element metadata, Dictionary configuration) throws ConfigurationException {

        Element[] behaviorElements = metadata.getElements(HandlerReference.BEHAVIOR_MANAGER_HANDLER,HandlerReference.NAMESPACE);

        if (behaviorElements == null) {
            throw new ConfigurationException("Behavior Elements are null ");
        }

        ProvidedService providedService = getContextEntityProvidedService(metadata);
        List<String> behaviorSpecs = new ArrayList<>();
        for (Element element:behaviorElements){
            Element[] behaviorIndividualElements = element.getElements(BehaviorReference.BEHAVIOR_INDIVIDUAL_ELEMENT_NAME,"");

            if (behaviorIndividualElements == null){
                throw new ConfigurationException("Behavior Individual Element is null ");
            }

            for (Element individualBehaviorElement:behaviorIndividualElements) {
                RequiredBehavior requiredBehavior = new RequiredBehavior(individualBehaviorElement.getAttribute(BehaviorReference.ID_ATTRIBUTE_NAME),
                        individualBehaviorElement.getAttribute(BehaviorReference.SPECIFICATION_ATTRIBUTE_NAME),
                        individualBehaviorElement.getAttribute(BehaviorReference.IMPLEMEMENTATION_ATTRIBUTE_NAME),
                        configuration,
                        this,
                        providedService,
                        getProvideServiceHandler()
                );
                myRequiredBehaviorById.put(individualBehaviorElement.getAttribute(BehaviorReference.ID_ATTRIBUTE_NAME),requiredBehavior);

                String fieldAttribute = individualBehaviorElement.getAttribute(BehaviorReference.FIELD_ATTRIBUTE_NAME);
                FieldMetadata fieldMetadata = null;
                if (fieldAttribute != null){
                    fieldMetadata = getPojoMetadata().getField(fieldAttribute);
                }
                if (fieldMetadata != null){
                    getInstanceManager().register(fieldMetadata,requiredBehavior.getBehaviorInterceptor());
                }

                behaviorSpecs.add( individualBehaviorElement.getAttribute(BehaviorReference.SPECIFICATION_ATTRIBUTE_NAME));
            }
        }

        /**Due to service controller issue we must create a controller Always on true for contextEntitySpec**/
        createControllerForContextEntity(metadata,behaviorSpecs);
    }

    private void createControllerForContextEntity(Element metadata,List<String> behaviorSpecs){
        ProvidedService providedService = getContextEntityProvidedService(metadata);
        int i=0;
        for(String spec:getContextEntitySpec(metadata)){
            if (!behaviorSpecs.contains(spec)){
                providedService.setController(CONTEXT_ENTITY_CONTROLLER_FIELD_NAME+i,true,spec);
                i++;
            }
        }
    }

    private ProvidedService getContextEntityProvidedService(Element metadata){
        String[] contextEntitySpecs = getContextEntitySpec(metadata);
        ProvidedServiceHandler providedServiceHandler = getProvideServiceHandler();
        ProvidedService[] providedServices = providedServiceHandler.getProvidedServices();
        for (ProvidedService providedService:providedServices){
            String[] serviceSpecifications = providedService.getServiceSpecifications();
            if (compare(contextEntitySpecs,serviceSpecifications)){
                return providedService;
            }
        }
        return null;
    }

    private String[] getContextEntitySpec(Element metadata){
        Element[] providesElements = metadata.getElements("provides");
        for (Element provides : providesElements){
            Element[] propertyElements = provides.getElements("property");
            for (Element property : propertyElements){
                String name = property.getAttribute("name");
                if (ContextEntity.ENTITY_CONTEXT_SERVICES.equals(name)){
                    return ParseUtils.parseArrays(provides.getAttribute("specifications"));
                }
            }
        }
        return null;
    }

    private boolean compare(String[] contextEntitySpecs,String[] providedServiceSpecs){
        for (String contextEntitySpec : contextEntitySpecs){
            boolean find = false;
            for (String providedServiceSpec : providedServiceSpecs){
                if (contextEntitySpec.equals(providedServiceSpec)){
                    find = true;
                    continue;
                }
            }
            if (!find){
                return false;
            }
        }
        return true;
    }

    @Override
    public  synchronized void stop() {
        for (Map.Entry<String,RequiredBehavior> entry : myRequiredBehaviorById.entrySet()){
            entry.getValue().tryDispose();
        }
        myRequiredBehaviorById.clear();
    }

    @Override
    public void start() {
        //Do nothing
    }


    private ProvidedServiceHandler getProvideServiceHandler(){
        return (ProvidedServiceHandler) getHandler(HandlerFactory.IPOJO_NAMESPACE + ":provides");
    }

    /**
     * Issue : behavior must be deactivate before instance become Invalid ...
     */
    @Override
    public synchronized void stateChanged(int newState) {
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

    @Bind(id = "behaviorF",specification = Factory.class,optional = true,proxy = false,aggregate = true,filter = "("+BehaviorReference.BEHAVIOR_FACTORY_TYPE_PROPERTY +"="+BehaviorReference.BEHAVIOR_FACTORY_TYPE_PROPERTY_VALUE +")")
    public synchronized void bindBehaviorFactory(Factory behaviorFactory, Map prop){
        for (Map.Entry<String,RequiredBehavior> entry : myRequiredBehaviorById.entrySet()){
            if (match(entry.getValue(),prop)){
                entry.getValue().setFactory(behaviorFactory);
                entry.getValue().addManager();
                entry.getValue().registerBehaviorListener(this);
                if (getInstanceManager().getState() == ComponentInstance.VALID){
                    entry.getValue().tryStartBehavior();
                }
            }
        }
    }

    @Unbind(id = "behaviorF")
    public synchronized void unbindBehaviorFactory(Factory behaviorFactory,Map prop){
        for (Map.Entry<String,RequiredBehavior> entry : myRequiredBehaviorById.entrySet()){
            if (match(entry.getValue(),prop)){
                entry.getValue().unRef();
            }
        }
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

    @Override
    public void behaviorStateChange(int state) {

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
