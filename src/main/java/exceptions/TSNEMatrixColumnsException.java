package exceptions;

public class TSNEMatrixColumnsException extends RNAScoopException {

    public TSNEMatrixColumnsException(int lineNumber) {
        message = "Line " + lineNumber + " in the t-SNE matrix does not have 2 columns";
    }

}
