package exceptions;

public class LabelSetMissingPathException extends RNAScoopException {

    public LabelSetMissingPathException() {
        message = "Must include paths for all label sets";
    }

}
