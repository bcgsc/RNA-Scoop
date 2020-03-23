package exceptions;

public class TSNEDataFileNotFoundException extends RNAScoopException {

    public TSNEDataFileNotFoundException() {
        message = "Could not find t-SNE data file";
    }

}
