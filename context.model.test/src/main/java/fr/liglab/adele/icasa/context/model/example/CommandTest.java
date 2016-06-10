package fr.liglab.adele.icasa.context.model.example;


import fr.liglab.adele.cream.event.handler.annotation.ContextUpdate;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;

import java.util.Hashtable;

@Component(immediate = true)
@Instantiate
@Provides(specifications = CommandTest.class)
public class CommandTest {

    @ServiceProperty(name = "osgi.command.scope", value = "cream")
    String m_scope;

    @Requires(optional = false,specification = Factory.class,filter = "(factory.name=fr.liglab.adele.icasa.context.model.example.ContextEntityImpl)")
    Factory contextEntityFacto ;

    @Requires(specification = ContextEntityDescription.class,id = "test", optional = true,proxy = false)
    ContextEntityDescription description ;

    @Requires(specification = BehaviorS.class,id = "test2", optional = true,proxy = false)
    BehaviorS behaviorS ;

    @ContextUpdate(specification = ContextEntityDescription.class,stateId = ContextEntityDescription.HELLO)
    public void updateState(ContextEntityDescription entityDescription,Object newP,Object old){
        System.out.println(" change on Hello catch");
    }

    @Bind(id = "test")
    public void bindTest(ContextEntityDescription description){
        System.out.println(description.hello());
    }

    /**
     * Defines the functions (commands).
     */
    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] m_function = new String[]{"create","testBehavior","testEvent"};


    @Descriptor("Create A Entity")
    public void create(@Descriptor("create") String... handleId) {
        try {
            String id;
            if (handleId.length < 1) {
                System.out.println("Need At least 1 arg.....");
            } else {
                id = handleId[0];
                Hashtable prop = new Hashtable();
                prop.put("context.entity.id",id);
                contextEntityFacto.createComponentInstance(prop);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Descriptor("Create A Entity")
    public void testBehavior(@Descriptor("testBehavior") String... handleId) {
        try {
            System.out.println((behaviorS.coucou()));
            System.out.println(((ContextEntityDescription)behaviorS).hello());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Descriptor("Create A Entity")
    public void testEvent(@Descriptor("testEvent") String... handleId) {
        try { description.setHello(" cuicui ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
