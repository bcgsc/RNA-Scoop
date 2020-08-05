package exceptions;

public class InvalidMaxIterationsException extends RNAScoopException {

    public InvalidMaxIterationsException() {
        message = "Max number of iterations must be a non-negative integer";
    }

}
