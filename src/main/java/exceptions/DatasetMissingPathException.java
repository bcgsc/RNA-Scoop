package exceptions;

public class DatasetMissingPathException extends RNAScoopException {

    public DatasetMissingPathException(String fileName) {
        message = "Must include path to " + fileName;
    }

}
