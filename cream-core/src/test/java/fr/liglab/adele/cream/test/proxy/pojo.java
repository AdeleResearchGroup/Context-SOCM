package fr.liglab.adele.cream.test.proxy;

/**
 * Created by aygalinc on 09/09/16.
 */
public class pojo implements ServiceH {


    @Override
    public void setSomething() {
        System.out.println(" coucou ");
    }

    @Override
    public void getSomething() {
        System.out.println(" coucou 2");
    }

    @Override
    public void doSomething() {
        System.out.println(" coucou 3");
    }
}
