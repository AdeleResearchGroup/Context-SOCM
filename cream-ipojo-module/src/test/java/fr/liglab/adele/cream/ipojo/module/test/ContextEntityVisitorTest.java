package fr.liglab.adele.cream.ipojo.module.test;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.ipojo.module.AnnotationBuilder;
import fr.liglab.adele.cream.ipojo.module.ContextEntityProcessor;
import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by aygalinc on 26/08/16.
 */
public class ContextEntityVisitorTest extends UtilityProcessorTest{

    /*
    	 * The loader used to load the classes referenced in annotations. Notice that we try to load classes using
    	 * the class loader of this module, and if it is not possible we use a loader that creates an empty mocked-up
    	 * class that represent the referenced class.
    	 */
    ClassLoader classReferenceLoader = new AnnotationBuilder.ClassReferenceLoader(this.getClass().getClassLoader());

    @Test
    public void testDefaultNameIsClassname() throws Exception {

        BindingContext context = this.createTestWorkbench(FakeComponentClass.class);
        ContextEntityProcessor processorFactory = new ContextEntityProcessor(classReferenceLoader);
        ContextEntity contextEntity = new ContextEntityImpl();
        processorFactory.process(context,contextEntity);

        assertNotNull(root);
        assertEquals(FakeComponentClass.class.getName(), root.getAttribute("classname"));
    }

    private class ContextEntityImpl extends AbstractAnnotationTest implements ContextEntity{

        @Override
        public Class<?>[] services() {
            return new Class<?>[]{FakeComponentClass.class};
        }
    }

    private class FakeComponentClass {

    }

}
