package exceptions;

public class InvalidFigureScaleException extends RNAScoopException {

    public InvalidFigureScaleException() {
        message = "Scale must be number greater than 0";
    }

}
