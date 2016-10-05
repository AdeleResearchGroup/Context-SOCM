package fr.liglab.adele.cream.it.facilities.requirement;

import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import org.apache.felix.ipojo.annotations.*;

@Component(immediate = true)
@Provides(specifications = BindCounterService.class)
/**
 * A is declared as the union class, the class order in union is important because ipojo check if the declared specification attribute correspond to the
 * dependency field type. The hack is possible because ipojo compare the value of spec to the first class of the union in dep handler.
 */
public class ContextConsumer<A extends ContextProvideService & BehaviorService> implements BindCounterService {


    int unbind = 0;

    int bind=0;

    @Requires(id = "ContextReq",specification = ContextProvideService.class,optional = true,proxy = false)
    @ContextRequirement(spec = {BehaviorService.class})
    ContextProvideService service;

    @Bind(id = "ContextReq" )
    public synchronized void bindService(ContextProvideService service){
        bind++;
    }

    @Unbind(id = "ContextReq" )
    public synchronized void unbindService(ContextProvideService service){
        unbind++;
    }

    @Validate
    public void validate(){

    }


    @Invalidate
    public void invalidate(){

    }

    @Override
    public synchronized int getBind() {
        return bind;
    }

    @Override
    public synchronized int getUnbind() {
        return unbind;
    }



    @Requires(id = "ContextReq2",specification = ContextProvideService.class,optional = true,proxy = false)
    @ContextRequirement(spec = {BehaviorService.class})
    A serviceGeneric;


    @Override
    public void callGenericBind() {
        serviceGeneric.getFalse();
        serviceGeneric.getTrue();
    }
}
