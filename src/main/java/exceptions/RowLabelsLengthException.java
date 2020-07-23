package exceptions;

public class RowLabelsLengthException extends RNAScoopException {

    public RowLabelsLengthException() {
        message = "The number of row labels does not equal the number of rows in the matrix";
    }

}
