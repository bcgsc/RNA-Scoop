package exceptions;

public class TSNeDataFileNotFoundException extends RNAScoopException {

    public TSNeDataFileNotFoundException() {
        message = "Could not find t-SNE data file";
    }

}
