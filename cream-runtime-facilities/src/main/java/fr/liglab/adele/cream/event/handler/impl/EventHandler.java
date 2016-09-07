package fr.liglab.adele.cream.event.handler.impl;

import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextUpdate;
import fr.liglab.adele.cream.facilities.ipojo.annotation.FacilitiesHandlerReference;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.dependency.interceptors.DependencyInterceptor;
import org.apache.felix.ipojo.dependency.interceptors.ServiceTrackingInterceptor;
import org.apache.felix.ipojo.dependency.interceptors.TransformedServiceReference;
import org.apache.felix.ipojo.handlers.dependency.Dependency;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.util.DependencyModel;
import org.apache.felix.ipojo.util.Log;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Handler(name = FacilitiesHandlerReference.EVENT_HANDLER_NAME, namespace = FacilitiesHandlerReference.FACILITIES_HANDLER_NAMESPACE)
@Provides(specifications = ServiceTrackingInterceptor.class)
public class EventHandler extends PrimitiveHandler implements ServiceTrackingInterceptor {

    /**
     * Filter used to match the dependencies we are interested in
     */
    @ServiceProperty(name= DependencyInterceptor.TARGET_PROPERTY)
    private String dependencyFilter;

    private final Map<DependencyModel,List<ContextUpdateElement>> myDependencyModelToContextElement = new ConcurrentHashMap<>();

    private final List<ContextUpdateElement> myContextUpdateElement = new ArrayList<>();

    @Override
    public <S> TransformedServiceReference<S> accept(DependencyModel dependency, BundleContext context, TransformedServiceReference<S> ref) {

        List<ContextUpdateElement> elements = myDependencyModelToContextElement.get(dependency);

        if (elements == null){
            return ref;
        }

        for (ContextUpdateElement element: elements){
            element.updateIfNecessary(ref);
        }

        return ref;
    }

    @Override
    public void open(DependencyModel dependency) {
        if (dependency instanceof Dependency) {
            List<ContextUpdateElement> contextUpdateElements = new ArrayList<>();
            for (ContextUpdateElement element : myContextUpdateElement){

                if (element.equals(dependency)){
                    contextUpdateElements.add(element);

                }

            }
            if (!contextUpdateElements.isEmpty()){
                myDependencyModelToContextElement.put(dependency,contextUpdateElements);
            }
        }
    }

    @Override
    public void close(DependencyModel dependency) {

    }

    @Override
    public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
        InstanceManager instanceManager = getInstanceManager();

        String instanceName				= instanceManager.getInstanceName();
        dependencyFilter				= "("+ Factory.INSTANCE_NAME_PROPERTY+"="+instanceName+")";

        Element[] handlerMethods = metadata.getElements(FacilitiesHandlerReference.EVENT_HANDLER_NAME, FacilitiesHandlerReference.FACILITIES_HANDLER_NAMESPACE);

        /*
         * Configure the list of handled fields
         */
        for (Element event: handlerMethods) {
            String methodEvent = event.getAttribute("method");
            String spec = event.getAttribute(ContextUpdate.SPECIFICATION_ATTRIBUTE);
            String state = event.getAttribute(ContextUpdate.STATE_ID_ATTRIBUTE);
            MethodMetadata methodMetadata = getPojoMetadata().getMethod(methodEvent);
            if (checkMethodStructure(methodMetadata)){
                myContextUpdateElement.add(new ContextUpdateElement(spec,state,methodMetadata,instanceManager));
            }else {
                getLogger().log(Log.WARNING," Method " + methodEvent + " is malformed");
            }
        }
    }

    /**
     * Not check the first type parameter because it cannot be done without loading the pojo to deduce interface Hierarchy of the first parameter type
     */
    private boolean checkMethodStructure(MethodMetadata metadata){
        String argument[] = metadata.getMethodArguments();
        if (argument == null || argument.length != 3){
            return false;
        }

        if (! argument[1].equals(Object.class.getName())){
            return false;
        }

        if (! argument[2].equals(Object.class.getName())){
            return false;
        }
        return true;

    }

    @Override
    public  void stop() {
        for (Map.Entry<DependencyModel,List<ContextUpdateElement>> entry : myDependencyModelToContextElement.entrySet()){
            for (ContextUpdateElement updateElement : entry.getValue()){
                updateElement.clearValue();
            }
        }
        myDependencyModelToContextElement.clear();
    }

    @Override
    public  void start() {

    }
}
