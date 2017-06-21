package fr.liglab.adele.cream.administration.impl;

import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
import org.apache.felix.ipojo.dependency.interceptors.DefaultServiceTrackingInterceptor;
import org.apache.felix.ipojo.dependency.interceptors.TransformedServiceReference;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.ParseUtils;
import org.apache.felix.ipojo.util.DependencyModel;
import org.osgi.framework.BundleContext;

import java.util.List;

@Component(immediate = true)
@Instantiate
@Provides
public class AdministrationArchitectureTrackingInterceptor extends DefaultServiceTrackingInterceptor {

    @ServiceProperty
    private String target = "(instance.name=fr.liglab.adele.cream.administration.impl.AdministrationImpl-0)";

    @Override
    public <S> TransformedServiceReference<S> accept(DependencyModel dependency, BundleContext context,
                                                     TransformedServiceReference<S> ref) {

        Architecture serviceObj = null;
        boolean nullReturn = false;

        try {
            serviceObj = (Architecture) context.getService(ref.getWrappedReference());

            ComponentTypeDescription componentDescription= serviceObj.getInstanceDescription().getComponentDescription();

            Element componentElement = componentDescription.getDescription();

            Element[] requiredHandlerElements = componentElement.getElements("requiredhandlers");
            for (Element requiredHandlerElement:requiredHandlerElements){
                String list = requiredHandlerElement.getAttribute("list");
                List<String> requiresHandler = ParseUtils.parseArraysAsList(list);
                if (!requiresHandler.contains(HandlerReference.NAMESPACE+":"+HandlerReference.ENTITY_HANDLER)){
                    return null;
                }
            }
        } finally {
            if (serviceObj != null) {
                context.ungetService(ref.getWrappedReference());
            } else {
                nullReturn = true;
            }

        }

        if (nullReturn) {
            return null;
        }

        return ref;
    }
}
