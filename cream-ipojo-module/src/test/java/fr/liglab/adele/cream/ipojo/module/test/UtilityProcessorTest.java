package fr.liglab.adele.cream.ipojo.module.test;

import org.apache.felix.ipojo.manipulator.Reporter;
import org.apache.felix.ipojo.manipulator.metadata.annotation.ComponentWorkbench;
import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.apache.felix.ipojo.metadata.Element;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by aygalinc on 29/08/16.
 */
public abstract class UtilityProcessorTest {
    protected Element root;
    protected Element instance;

    public BindingContext createTestWorkbench(Class ComponentClassToTest){
        Reporter reporter = mock(Reporter.class);
        BindingContext context = mock(BindingContext.class);
        ComponentWorkbench workbench = mock(ComponentWorkbench.class);
        when(workbench.getType()).thenReturn(Type.getType(ComponentClassToTest));
        when(context.getWorkbench()).thenReturn(workbench);
        when(context.getReporter()).thenReturn(reporter);
        when(workbench.getClassNode()).thenReturn(new ClassNode());

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                root = (Element) invocation.getArguments()[0];
                return null;
            }
        }).when(workbench).setRoot(any(Element.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                instance = (Element) invocation.getArguments()[0];
                return null;
            }
        }).when(workbench).setInstance(any(Element.class));
        return context;
    }
}
