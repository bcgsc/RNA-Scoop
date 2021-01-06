package exceptions;

public class GradientMinGreaterEqualMaxException extends RNAScoopException {

    public GradientMinGreaterEqualMaxException() {
        message = "Gradient min must be less than the gradient max";
    }

}