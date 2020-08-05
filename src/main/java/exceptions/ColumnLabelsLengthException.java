package exceptions;

public class ColumnLabelsLengthException extends RNAScoopException {

    public ColumnLabelsLengthException() {
        message = "The number of column labels does not equal the number of columns in the expression matrix";
    }

}
