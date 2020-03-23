package exceptions;

public class TSNENegativeExpressionInMatrixException extends RNAScoopException {

    public TSNENegativeExpressionInMatrixException() {
        message = "Given matrix has negative isoform expression values. This is not allowed";
    }

}
