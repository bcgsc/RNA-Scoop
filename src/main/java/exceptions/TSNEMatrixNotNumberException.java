package exceptions;

public class TSNEMatrixNotNumberException extends RNAScoopException {

    public TSNEMatrixNotNumberException(String coordinates, int lineNumber) {
        message = "The coordinates " + coordinates + " at line " + lineNumber + " in the t-SNE matrix contain non-numerical values";
    }

}
