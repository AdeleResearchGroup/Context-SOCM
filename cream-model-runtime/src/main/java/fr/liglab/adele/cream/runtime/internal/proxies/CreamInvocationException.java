package fr.liglab.adele.cream.runtime.internal.proxies;

/**
 * Created by aygalinc on 25/08/16.
 */
@SuppressWarnings("serial")
public class CreamInvocationException extends RuntimeException {

    public CreamInvocationException() {
        super();
    }

    public CreamInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}