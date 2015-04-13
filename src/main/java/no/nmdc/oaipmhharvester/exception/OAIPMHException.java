package no.nmdc.oaipmhharvester.exception;

/**
 * OAI-PMH Exception thrown in the harvester
 *
 * @author sjurl
 */
public class OAIPMHException extends Exception {

    public OAIPMHException(String message) {
        super(message);
    }

    public OAIPMHException(String message, Exception ex) {
        super(message, ex);
    }
}
