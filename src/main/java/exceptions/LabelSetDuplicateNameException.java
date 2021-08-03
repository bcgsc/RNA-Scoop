package exceptions;

public class LabelSetDuplicateNameException extends RNAScoopException {

    public LabelSetDuplicateNameException() {
        message = "Two or more label sets have the same name; this is not allowed";
    }

}
