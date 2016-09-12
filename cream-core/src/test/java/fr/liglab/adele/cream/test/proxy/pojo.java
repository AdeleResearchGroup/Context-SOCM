package fr.liglab.adele.cream.test.proxy;

import fr.liglab.adele.cream.utils.ContextServiceTe;

/**
 * Created by aygalinc on 09/09/16.
 */
public class pojo  implements ContextService{


    @Override
    public void setSomething() {
        System.out.println(" coucou ");
    }

    @Override
    public void getSomething() {
        System.out.println(" coucou 2");
    }
}
