package exceptions;

public class TSNEMatrixLengthException extends RNAScoopException {

    public TSNEMatrixLengthException() {
        message = "The number of rows in the t-SNE matrix does not equal the number of rows in the expression matrix";
    }

}
