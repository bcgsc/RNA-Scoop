package exceptions;

/**
 * Custom main.java.exceptions made for the RNA-Scoop application extend this class
 */
public abstract class RNAScoopException extends Exception {
    protected String message;

    public void addToMessage(String addedMessage) {
        message += addedMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
