package exceptions;

public abstract class TSNEPlotException extends Exception {
    protected String message;

    @Override
    public String getMessage() {
        return message;
    }
}
