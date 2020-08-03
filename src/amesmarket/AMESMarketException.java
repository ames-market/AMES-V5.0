
package amesmarket;

/**
 * Represent some problem in the market.
 *
 *
 */
public class AMESMarketException extends Exception{

    private static final long serialVersionUID = 6790407400128954427L;

    public AMESMarketException() {
        super();
    }

    public AMESMarketException(String msg) {
        super(msg);
    }

    public AMESMarketException(Throwable cause) {
        super(cause);
    }

    public AMESMarketException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
