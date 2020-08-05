package exceptions;

public class NegativeExpressionInMatrixException extends RNAScoopException {

    public NegativeExpressionInMatrixException() {
        message = "Given expression matrix has negative isoform expression values. This is not allowed";
    }

}
