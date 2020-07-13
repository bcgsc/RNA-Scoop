package exceptions;

public class TSNEColumnLabelsLengthException extends RNAScoopException {

    public TSNEColumnLabelsLengthException() {
        message = "The number of column labels does not equal the number of columns in the matrix";
    }

}
