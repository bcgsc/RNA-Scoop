package exceptions;

public class TSNELabelsFileNotFoundException extends RNAScoopException {

    public TSNELabelsFileNotFoundException() {
        message = "Could not find t-SNE labels file";
    }

}
