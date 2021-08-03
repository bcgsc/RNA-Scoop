package exceptions;

public class LabelSetMissingNameException extends RNAScoopException {

    public LabelSetMissingNameException() {
        message = "Must include names for all label sets";
    }

}
