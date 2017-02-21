package fr.liglab.adele.cream.runtime.handler.functional.extension.tracker;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.internal.FunctionalExtensionReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.runtime.handler.functional.extension.lifecycle.FunctionalExtensionStateListener;
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

@Handler(name = HandlerReference.FUNCTIONAL_EXTENSION_TRACKER_HANDLER, namespace = HandlerReference.NAMESPACE,level = 1)
public class BehaviorTrackerHandler extends PrimitiveHandler implements InvocationHandler,FunctionalExtensionStateListener,ContextSource{

    private static final String[] NO_SPEC = {};

    private static final String CONTEXT_ENTITY_CONTROLLER_FIELD_NAME = " context.entity.controller.";

    private final Map<String,RequiredFunctionalExtension> myRequiredBehaviorById = new HashMap<>();

    private final Set<String> stateVariable = new ConcurrentSkipListSet<>();

    private final Set<String> mandatoryBehavior = new HashSet<>();

    private final ContextListener behaviorContextListener = new BehaviorEntityListener();

    private final Map<ContextListener,String[]> listeners = new ConcurrentHashMap<>();

    private final   List<String> behaviorSpecs = new ArrayList<>();

    private Element metadata;

    public ContextListener getBehaviorContextListener(){
        return behaviorContextListener;
    }

    /**
     * Configure part
     **/

    @Override
    public  void configure(Element metadata, Dictionary configuration) throws ConfigurationException {

        Element[] behaviorElements = metadata.getElements(HandlerReference.FUNCTIONAL_EXTENSION_TRACKER_HANDLER,HandlerReference.NAMESPACE);

        if (behaviorElements == null) {
            throw new ConfigurationException("FunctionalExtension Elements are null ");
        }

        for (Element element:behaviorElements){
            Element[] behaviorIndividualElements = element.getElements(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_INDIVIDUAL_ELEMENT_NAME.toString(),"");

            if (behaviorIndividualElements == null){
                throw new ConfigurationException("FunctionalExtension Individual Element is null ");
            }

            for (Element individualBehaviorElement:behaviorIndividualElements) {
                RequiredFunctionalExtension requiredFunctionalExtension = new RequiredFunctionalExtension(individualBehaviorElement.getAttribute(FunctionalExtensionReference.ID_ATTRIBUTE_NAME.toString()),
                        ParseUtils.parseArrays(individualBehaviorElement.getAttribute(FunctionalExtensionReference.SPECIFICATION_ATTRIBUTE_NAME.toString())),
                        individualBehaviorElement.getAttribute(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString()),
                        configuration,
                        this,
                        getProvideServiceHandler()
                );
                myRequiredBehaviorById.put(individualBehaviorElement.getAttribute(FunctionalExtensionReference.ID_ATTRIBUTE_NAME.toString()), requiredFunctionalExtension);

                String fieldAttribute = individualBehaviorElement.getAttribute(FunctionalExtensionReference.FIELD_ATTRIBUTE_NAME.toString());
                FieldMetadata fieldMetadata = null;
                if (fieldAttribute != null){
                    fieldMetadata = getPojoMetadata().getField(fieldAttribute);
                }
                if (fieldMetadata != null){
                    getInstanceManager().register(fieldMetadata, requiredFunctionalExtension.getExtensionInterceptor());

                }

                boolean mandatoryField = Boolean.parseBoolean(individualBehaviorElement.getAttribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_MANDATORY_ATTRIBUTE_NAME.toString()));
                if (mandatoryField){
                    mandatoryBehavior.add(individualBehaviorElement.getAttribute(FunctionalExtensionReference.ID_ATTRIBUTE_NAME.toString()));
                }
                behaviorSpecs.add( individualBehaviorElement.getAttribute(FunctionalExtensionReference.SPECIFICATION_ATTRIBUTE_NAME.toString()));
                for (Map.Entry<ContextListener,String[]> listenerEntry : listeners.entrySet()){
                    requiredFunctionalExtension.registerContextListener(listenerEntry.getKey(),listenerEntry.getValue());
                }
            }
        }


        if (!mandatoryBehavior.isEmpty()){
            setValidity(false);
        }

        this.metadata = metadata;
    }

    private void createControllerForContextEntity(Element metadata,List<String> behaviorSpecs){
        ProvidedService providedService = getContextEntityProvidedService(metadata);
        if(providedService == null){
            error("provided service is null, must not be happen ");
            return;
        }
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
        return NO_SPEC;
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

    /**
     * Lifecycle
     */

    @Override
    public  synchronized void stop() {
        for (Map.Entry<String,RequiredFunctionalExtension> entry : myRequiredBehaviorById.entrySet()){
            entry.getValue().tryDispose();
        }
        myRequiredBehaviorById.clear();
    }

    @Override
    public synchronized void start() {
        /**Due to service controller issue we must create a controller Always on true for contextEntitySpec**/
        createControllerForContextEntity(metadata,behaviorSpecs);

        /**
         * set allProvided service in order to create all the service controller link to behavior
         */
        ProvidedService providedService = getContextEntityProvidedService(metadata);
        for (Map.Entry<String,RequiredFunctionalExtension> requiredBehaviorEntry : myRequiredBehaviorById.entrySet()){
            requiredBehaviorEntry.getValue().setProvidedService(providedService);
        }

        for (String mandatoryBehaviorId : mandatoryBehavior){
            myRequiredBehaviorById.get(mandatoryBehaviorId).tryStartExtension();
        }
    }

    /**
     * Issue : behavior must be deactivate before instance become Invalid ...
     */
    @Override
    public synchronized void stateChanged(int newState) {
        if (newState == ComponentInstance.VALID){
            for (Map.Entry<String,RequiredFunctionalExtension> behavior: myRequiredBehaviorById.entrySet()){
                behavior.getValue().tryStartExtension();
            }
        }

        if (newState == ComponentInstance.INVALID){
            for (Map.Entry<String,RequiredFunctionalExtension> behavior: myRequiredBehaviorById.entrySet()){
                behavior.getValue().tryInvalid();
            }
        }
    }

    /**
     * Method used in changeOn process, behavior lifecyle handler are registered as context listener of ContextEntity Handler
     */

    public void registerContextEntityContextListener(ContextListener contextListener,String[] properties){
        ContextSource handler = (ContextSource)getHandler(HandlerReference.NAMESPACE + ":"+HandlerReference.ENTITY_HANDLER);
        if (handler != null){
            handler.registerContextListener(contextListener,properties);
        }
    }

    public void unregisterContextEntityContextListener(ContextListener contextListener){
        ContextSource handler = (ContextSource)getHandler(HandlerReference.NAMESPACE + ":"+HandlerReference.ENTITY_HANDLER);
        if (handler != null){
            handler.unregisterContextListener(contextListener);
        }
    }


    private ProvidedServiceHandler getProvideServiceHandler(){
        return (ProvidedServiceHandler) getHandler(HandlerFactory.IPOJO_NAMESPACE + ":provides");
    }


    /**
     * FunctionalExtension factory tracking part
     */

    @Bind(id = "behaviorF",specification = Factory.class,optional = true,proxy = false,aggregate = true,filter = "("+ FunctionalExtensionReference.FUNCTIONAL_EXTENSION_FACTORY_TYPE_PROPERTY +"="+ FunctionalExtensionReference.FUNCTIONAL_EXTENSION_FACTORY_TYPE_PROPERTY_VALUE +")")
    public synchronized void bindBehaviorFactory(Factory behaviorFactory, Map prop){
        for (Map.Entry<String,RequiredFunctionalExtension> entry : myRequiredBehaviorById.entrySet()){
            if (match(entry.getValue(),prop)){
                entry.getValue().setFactory(behaviorFactory);
                entry.getValue().addManager();
                if (getInstanceManager().getState() == ComponentInstance.VALID){
                    entry.getValue().tryStartExtension();
                }
            }
        }
    }

    @Unbind(id = "behaviorF")
    public synchronized void unbindBehaviorFactory(Factory behaviorFactory,Map prop){
        for (Map.Entry<String,RequiredFunctionalExtension> entry : myRequiredBehaviorById.entrySet()){
            if (match(entry.getValue(),prop)){
                entry.getValue().unRef();
            }
        }
    }

    protected boolean match(RequiredFunctionalExtension req, Map prop) {
        String[] specs = (String[]) prop.get(FunctionalExtensionReference.SPECIFICATION_ATTRIBUTE_NAME.toString());
        String impl = (String) prop.get(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString());
        boolean specMatch = true;
        List<String> listOfSpec = Arrays.asList(specs);

        for (String spec : req.getSpecName()){
            if(!listOfSpec.contains(spec)){
                specMatch = false;
            }
        }
        return   specMatch && req.getImplName().equalsIgnoreCase(impl);
    }

    /**
     * Method Invocation Delegation, linked to context provided strategy
     */


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        for (Map.Entry<String,RequiredFunctionalExtension> behaviorEntry : myRequiredBehaviorById.entrySet()){
            Object returnObj = behaviorEntry.getValue().invoke(proxy,method,args);
            if (SuccessorStrategy.NO_FOUND_CODE.equals(returnObj)){
                continue;
            }
            return returnObj;
        }
        return SuccessorStrategy.NO_FOUND_CODE;
    }



    /**
     * Context Source Implementation, facade for requires handler, this method can be called before configure so a listener list must be keept !
     */

    @Override
    public Object getProperty(String property) {
        for (Map.Entry<String,RequiredFunctionalExtension> requiredBehaviorEntry : myRequiredBehaviorById.entrySet()){
            Object prop = requiredBehaviorEntry.getValue().getProperty(property);
            if (prop != null){
                return prop;
            }
        }
        return null;
    }

    @Override
    public Dictionary getContext() {
        Map hashMap = new HashMap<>();
        for (Map.Entry<String,RequiredFunctionalExtension> requiredBehaviorEntry : myRequiredBehaviorById.entrySet()){
            hashMap.putAll((Map) requiredBehaviorEntry.getValue().getContext());
        }
        return new Hashtable<>(hashMap);
    }

    @Override
    public void registerContextListener(ContextListener listener, String[] properties) {
        listeners.put(listener,properties);
        for (Map.Entry<String,RequiredFunctionalExtension> requiredBehaviorEntry : myRequiredBehaviorById.entrySet()){
            requiredBehaviorEntry.getValue().registerContextListener(listener,properties);
        }

    }

    @Override
    public void unregisterContextListener(ContextListener listener) {
        for (Map.Entry<String,RequiredFunctionalExtension> requiredBehaviorEntry : myRequiredBehaviorById.entrySet()){
            requiredBehaviorEntry.getValue().unregisterContextListener(listener);
        }

        listeners.remove(listener);
    }

    /**
     * Context Listener Implem
     */
    /**
     * TODO : maybe some value are not pushed in case of behavior is started and component not, maybe try to cache this value,
     * or when component start perform an update of all value.
     */
    private class BehaviorEntityListener implements ContextListener{
        @Override
        public synchronized void update(ContextSource contextSource, String s, Object o) {

            ProvidedServiceHandler providerHandler = getProvideServiceHandler();
            if (providerHandler == null){
                return;
            }

            if (getInstanceManager().getState() != ComponentInstance.VALID){
                return;
            }


            if (o == null){
                Map<String,Object> propertyToRemove = new HashMap<>();
                propertyToRemove.put(s, "");
                if(stateVariable.contains(s)){
                    stateVariable.remove(s);
                    getProvideServiceHandler().removeProperties(new Hashtable<>(propertyToRemove));
                }
                return;
            }

            Map<String,Object> property = new HashMap<>();
            property.put(s, o);
            if (stateVariable.contains(s)){
                providerHandler.reconfigure(new Hashtable<>(property));
            }else {
                stateVariable.add(s);
                providerHandler.addProperties(new Hashtable<>(property));
            }

        }
    }

    /**
     * FunctionalExtension State Listener Implementation
     */

    @Override
    public void functionalExtensionStateChange(int state, String id) {
        if (ComponentInstance.INVALID == state){
            if (mandatoryBehavior.contains(id)){
                setValidity(false);
            }
        }
        if (ComponentInstance.VALID == state){
            if (checkRequiredBehavior()){
                setValidity(true);
            }
        }
    }

    private boolean checkRequiredBehavior(){
        for (String mandatoryBehaviorId : mandatoryBehavior){
            if(!myRequiredBehaviorById.get(mandatoryBehaviorId).isValid()){
                return false;
            }
        }
        return true;
    }


    /**
     * FunctionalExtension handler Description
     */

    @Override
    public HandlerDescription getDescription() {
        return new BehaviorHandlerDescription();
    }

    public class BehaviorHandlerDescription extends HandlerDescription {

        public BehaviorHandlerDescription(){
            super(BehaviorTrackerHandler.this);
        }

        @Override
        public Element getHandlerInfo() {
            Element element = super.getHandlerInfo();
            for (Map.Entry<String,RequiredFunctionalExtension> entry : myRequiredBehaviorById.entrySet()){
                entry.getValue().getExtensionDescription(element);
            }
            return element;
        }
    }

}
