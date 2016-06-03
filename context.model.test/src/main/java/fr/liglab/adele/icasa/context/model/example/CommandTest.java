package fr.liglab.adele.icasa.context.model.example;

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

    @Requires(id = "test", optional = true)
    ContextEntityDescription description ;

    @Bind(id = "test")
    public void bindTest(ContextEntityDescription description){
    description.hello();
    }

    /**
     * Defines the functions (commands).
     */
    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] m_function = new String[]{"create"};


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

}
