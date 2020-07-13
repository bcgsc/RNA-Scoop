package exceptions;

public class TSNERowLabelsLengthException extends RNAScoopException {

    public TSNERowLabelsLengthException() {
        message = "The number of row labels does not equal the number of rows in the matrix";
    }

}
