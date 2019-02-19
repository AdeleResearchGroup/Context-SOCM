package fr.liglab.adele.cream.test.dispatcher;

/**
 * Created by aygalinc on 09/09/16.
 */
public class Pojo implements ContextService, ExtendedService, ExtendedService.Nested {

	private int something;

	private boolean didSomtheing = false;
	
	private boolean didSomethingElse = false;
	
    @Override
    public void setSomething(int value) {
    	something = value;
    }

    @Override
    public int getSomething() {
        return something;
    }

    @Override
    public void doSomething() {
    	didSomtheing = true;
    }

    public boolean didSomething() {
    	return didSomtheing;
    }

    @Override
	public void doSomethingElse() {
		didSomethingElse = true;
	}
    
    public boolean didSomethingElse() {
    	return didSomethingElse;
    }
}
