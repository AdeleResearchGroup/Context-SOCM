package fr.liglab.adele.cream.test.dispatcher;

import org.junit.Test;

import fr.liglab.adele.cream.runtime.internal.proxies.dispatcher.MethodDispatcher;
import fr.liglab.adele.cream.runtime.internal.proxies.dispatcher.MethodDispatcherGenerator;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class DispatcherTest {


    @Test
    public void testGeneration() throws Throwable {

        Pojo pojo = new Pojo();

    	for (Class<?> service : pojo.getClass().getInterfaces()) {

    		Class<MethodDispatcher> dispatcher = MethodDispatcherGenerator.generate(service);

    		assertNotNull("Dispatcher not generated", dispatcher);
    		System.out.println("generated class = "+dispatcher);
			
		}
    }

    @Test
    public void testUnknown() throws Throwable {

		MethodDispatcher dispatcher = MethodDispatcherGenerator.dispatcherFor(ContextService.class);

        Pojo pojo = new Pojo();
        int invalidMethodId = 6666;
        
        Object result = dispatcher.dispatch(pojo,invalidMethodId,new Object[] {});
        assertEquals("Dispatching using wrong identifier",MethodDispatcher.UNKNOWN_METHOD,result);

    }
   
    @Test(expected=ClassCastException.class)
    public void testUnimplemented() throws Throwable {

		MethodDispatcher dispatcher = MethodDispatcherGenerator.dispatcherFor(Runnable.class);
		System.out.println("generated class = "+dispatcher.getClass());

		Pojo pojo = new Pojo();
		dispatcher.dispatch(pojo,MethodDispatcher.id(Runnable.class.getMethod("run", new Class<?>[] {})),new Object[] {});
    }

    @Test
    public void testInvocation() throws Throwable {
    	
        Pojo pojo = new Pojo();
        int someValue = 3636;
        
    	for (Class<?> service : pojo.getClass().getInterfaces()) {

    		MethodDispatcher dispatcher = MethodDispatcherGenerator.dispatcherFor(service);
    		System.out.println("Dispatching methods of "+service.getName());
    		System.out.println("generated class = "+dispatcher.getClass());

    		for (Method method : service.getMethods()) {
            	
                Object result = dispatcher.dispatch(pojo, method, new Object[] {someValue});
                System.out.println("result of "+method.getDeclaringClass()+"."+method.getName()+"= "+result);
            }

		}

    	assertEquals("Error dispatching getter/setter", pojo.getSomething(),someValue);
    	assertTrue("Error dipatching extended service", pojo.didSomething());
    	assertTrue("Error dipatching nested srevice",pojo.didSomethingElse());
    }

}
