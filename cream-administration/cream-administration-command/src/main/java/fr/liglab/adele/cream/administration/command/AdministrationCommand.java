package fr.liglab.adele.cream.administration.command;


import fr.liglab.adele.cream.administration.api.AdministrationService;
import fr.liglab.adele.cream.administration.api.ImmutableContextEntity;
import fr.liglab.adele.cream.administration.api.ImmutableContextState;
import fr.liglab.adele.cream.administration.api.ImmutableFunctionalExtension;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Component(immediate = true)
@Instantiate
@Provides(specifications = AdministrationCommand.class)
public class AdministrationCommand {

    @ServiceProperty(name = "osgi.command.scope", value = "cream-admin")
    String m_scope;

    @Requires(specification = AdministrationService.class, id = "creamAdministrationService", optional = false, proxy = false)
    AdministrationService administrationService;

    /**
     * Defines the functions (commands).
     */
    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] m_function = new String[]{"getContextEntities"};

    private static final Logger LOG = LoggerFactory.getLogger(AdministrationCommand.class);


    @Descriptor("Create A Entity")
    public void getContextEntities(@Descriptor("getContextEntities") String... handleId) {
       try {

           Set<ImmutableContextEntity> contextEntities =  administrationService.getContextEntities();

           for (ImmutableContextEntity entity : contextEntities){
               System.out.println("[--------------------------------------------------------------");
               System.out.println("Context Entity : " + entity.getId());

               System.out.println("\t-> Core :");
               System.out.println("\t\t+State : "+entity.getState());
               System.out.println("\t\t+manages : ");
               for (ImmutableContextState contextState: entity.getContextStates()){
                   System.out.print("\t\t\tContext-State : " + contextState.getId() +" , value = " + contextState.getValue());
                   if (contextState.getSynchroPeriod() !=null){
                       System.out.print(" , synchronisation period : [ " + contextState.getSynchroPeriod().getPeriod() + " , " + contextState.getSynchroPeriod().getUnit()+" ]" );
                   }
                   System.out.println("");
               }

               System.out.println("\t-> Functional-Extensions : ");
               for (ImmutableFunctionalExtension extension: entity.getExtensions()){
                   System.out.println("\t\t--Functional-Extension : " + extension.getId());
                   System.out.println("\t\t\t+State : "+extension.getState());
                   System.out.println("\t\t\t+provides : " + extension.getSpecifications());
                   System.out.println("\t\t\t+manages : ");
                   for (ImmutableContextState contextState: extension.getContextStates()){
                       System.out.print("\t\t\t\tContext-State : " + contextState.getId() +" , value = " + contextState.getValue());
                       if (contextState.getSynchroPeriod() !=null){
                           System.out.print(" , synchronisation period : [ " + contextState.getSynchroPeriod().getPeriod() + " , " + contextState.getSynchroPeriod().getUnit()+" ]" );
                       }
                       System.out.println("");
                   }
               }
               System.out.println("--------------------------------------------------------------]");
               System.out.println("");
           }
       }catch (Exception e){
           LOG.error("exception occurs during command execution",e);
       }
    }

}
