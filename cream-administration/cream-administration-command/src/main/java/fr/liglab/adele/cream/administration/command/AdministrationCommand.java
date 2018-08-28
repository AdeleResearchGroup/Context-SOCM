package fr.liglab.adele.cream.administration.command;


import fr.liglab.adele.cream.administration.api.*;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    String[] m_function = new String[]{"contextEntities","contextEntity","reconfigureSynchronisationFrequency","reconfigureFunctionalExtension","creamAdminHelp"};

    private static final Logger LOG = LoggerFactory.getLogger(AdministrationCommand.class);


    @Descriptor("Get a list of all context entities")
    public void contextEntities(@Descriptor("contextEntities") String... handleId) {
        try {

            Set<ImmutableContextEntity> contextEntities =  administrationService.getContextEntities();

            for (ImmutableContextEntity entity : contextEntities){
                System.out.print("+ Context Entity : " + entity.getId());

                System.out.print(" Core : [" + entity.getState()+"]");

                for (ImmutableFunctionalExtension extension: entity.getExtensions()){
                    System.out.print(" ," + extension.getId());
                    System.out.print(" : ["+extension.getState()+"]");
                }
                System.out.println("");
            }
        }catch (Exception e){
            LOG.error("exception occurs during command execution",e);
        }
    }

    @Descriptor("Get a description of a given context entity")
    public void contextEntity(@Descriptor("contextEntity") String... handleId) {
        try {
            if (handleId == null || handleId.length == 0){
                System.out.println("Error : you must indicate a context entity ID");
                return;
            }

            String contextId = handleId[0];
            boolean find = false;

            Set<ImmutableContextEntity> contextEntities =  administrationService.getContextEntities();

            for (ImmutableContextEntity entity : contextEntities){
                if (!entity.getId().equals(contextId)){
                    continue;
                }
                find = true;

                System.out.println("[--------------------------------------------------------------");
                System.out.println("Context Entity : " + entity.getId());
                ImmutableCore core = entity.getCore();
                System.out.println("\t-> Core :");
                System.out.println("\t\t+State : "+entity.getState());
                System.out.println("\t\t+Implements : "+core.getImplementedSpecifications());
                System.out.println("\t\t+Manages : ");
                for (ImmutableContextState contextState: core.getContextStates()){
                    System.out.print("\t\t\tContext-State : " + contextState.getId() +" , value = " + contextState.getValue());
                    if (contextState.getSynchroPeriod() !=null){
                        System.out.print(" , synchronisation period : [ " + contextState.getSynchroPeriod().getPeriod() + " , " + contextState.getSynchroPeriod().getUnit()+" ]" );
                    }
                    System.out.println("");
                }
                for (ImmutableRelation relation: core.getRelations()){
                    System.out.print("\t\t\tRelation : state : " + relation.getState() +" , source = " + relation.getSourcesId());

                    System.out.println("");
                }

                System.out.println("\t-> Functional-Extensions : ");
                for (ImmutableFunctionalExtension extension: entity.getExtensions()){
                    System.out.println("\t\t--Functional-Extension : " + extension.getId());
                    System.out.println("\t\t\t+IsMandatory : "+extension.isMandatory());
                    System.out.println("\t\t\t+Provides : " + extension.getManagedSpecifications());
                    System.out.println("\t\t\t+IsInstantiate : "+extension.isInstantiate());
                    if ("false".equals(extension.isInstantiate())){
                        continue;
                    }
                    System.out.println("\t\t\t+State : "+extension.getState());
                    System.out.println("\t\t\t+Implements : " + extension.getImplementedSpecifications());
                    System.out.println("\t\t\t+Manages : ");
                    for (ImmutableContextState contextState: extension.getContextStates()){
                        System.out.print("\t\t\t\tContext-State : " + contextState.getId() +" , value = " + contextState.getValue());
                        if (contextState.getSynchroPeriod() !=null){
                            System.out.print(" , synchronisation period : [ " + contextState.getSynchroPeriod().getPeriod() + " , " + contextState.getSynchroPeriod().getUnit()+" ]" );
                        }
                        System.out.println("");
                    }
                    for (ImmutableRelation relation: extension.getRelations()){
                        System.out.print("\t\t\t\tRelation : state : " + relation.getState() +" , source = " + relation.getSourcesId());
                        System.out.println("");
                    }
                }
                System.out.println("--------------------------------------------------------------]");
                System.out.println("");
                break;
            }

            if (!find){
                System.out.println("No context entity can be find with the id  " + contextId);
            }
        }catch (Exception e){
            LOG.error("exception occurs during command execution",e);
        }
    }

    @Descriptor("Reconfigure a functional extension for a given context entity")
    public void reconfigureFunctionalExtension(@Descriptor("reconfigureFunctionalExtension") String... handleId) {
        try {

            if (handleId == null || handleId.length != 3){
                System.out.println("Error : you must provide three parameter {contextEntityId}, {functionalExtensionId} and {FunctionalExtensionNewClassname}");
                return;
            }

            administrationService.reconfigureContextEntityComposition(handleId[0],handleId[1],handleId[2]);

        }catch (Exception e){
            LOG.error("exception occurs during command execution",e);
        }
    }

    @Descriptor("Reconfigure the synchronisation frequency of a given state variable of a given context entity")
    public void reconfigureSynchronisationFrequency(@Descriptor("reconfigureSynchronisationFrequency") String... handleId) {
        try {

            if (handleId == null || handleId.length != 4){
                System.out.println("Error : you must provide three parameter {contextEntityId}, {contextStateId}, {newFrequency} and {newUnit}");
                return;
            }

            administrationService.reconfigureContextEntityFrequency(handleId[0],handleId[1],Long.valueOf(handleId[2]), TimeUnit.valueOf(handleId[3]));

        }catch (Exception e){
            LOG.error("exception occurs during command execution",e);
        }
    }

    @Descriptor("Show help about cream-admin command")
    public void creamAdminHelp(@Descriptor("creamAdminHelp") String... handleId) {
        try {
            System.out.print("\tCommand name: contextEntities");
            System.out.println("");
            System.out.print("\t\tDescription : Get a list of all context entities");
            System.out.println("");
            System.out.println("");

            System.out.print("\tCommand name: contextEntity");
            System.out.println("");
            System.out.print("\t\tDescription : Get a description of a given context entity");
            System.out.println("");
            System.out.print("\t\tParameters : ");
            System.out.println("");
            System.out.print("\t\t\t{ContextEntityId} : Id of a context entity");
            System.out.println("");
            System.out.println("");

            System.out.print("\tCommand name: reconfigureFunctionalExtension");
            System.out.println("");
            System.out.print("\t\tDescription : Reconfigure a functional extension for a given context entity");
            System.out.println("");
            System.out.print("\t\tParameters : ");
            System.out.println("");
            System.out.print("\t\t\t{ContextEntityId} : Id of a context entity");
            System.out.println("");
            System.out.print("\t\t\t{FunctionalExtensionId} : Id of the functional entity to reconfigure");
            System.out.println("");
            System.out.print("\t\t\t{FunctionalExtensionNewClassname} : Classname of the new functional extension");
            System.out.println("");
            System.out.println("");

            System.out.print("\tCommand name: reconfigureSynchronisationFrequency");
            System.out.println("");
            System.out.print("\t\tDescription : Reconfigure the synchronisation frequency of a given state variable of a given context entity");
            System.out.println("");
            System.out.print("\t\tParameters : ");
            System.out.println("");
            System.out.print("\t\t\t{ContextEntityId} : Id of a context entity");
            System.out.println("");
            System.out.print("\t\t\t{contextStateId} : Id of the state variable to reconfigure");
            System.out.println("");
            System.out.print("\t\t\t{newFrequency} : New frequency duration");
            System.out.println("");
            System.out.print("\t\t\t{newUnit} : New frequency unit");
            System.out.println("");

        }catch (Exception e){
            LOG.error("exception occurs during command execution",e);
        }
    }
}
