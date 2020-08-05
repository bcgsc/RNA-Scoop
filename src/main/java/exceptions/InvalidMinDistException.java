package exceptions;

public class InvalidMinDistException extends RNAScoopException {

    public InvalidMinDistException() {
        message = "Min distance must be a number in range [0, 1]";
    }

}
