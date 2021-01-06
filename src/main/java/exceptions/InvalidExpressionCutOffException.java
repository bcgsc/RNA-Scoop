package exceptions;

public class InvalidExpressionCutOffException extends RNAScoopException {

    public InvalidExpressionCutOffException() {
        message = "Expression cutoffs must be non-negative numbers";
    }

}
