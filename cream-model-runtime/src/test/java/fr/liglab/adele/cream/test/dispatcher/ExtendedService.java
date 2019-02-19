package fr.liglab.adele.cream.test.dispatcher;

/**
 * Created by aygalinc on 13/09/16.
 */
public interface ExtendedService extends ContextService {

    public void doSomething();
    
    public interface Nested {
    	
        public void doSomethingElse();
    	
    }

}
