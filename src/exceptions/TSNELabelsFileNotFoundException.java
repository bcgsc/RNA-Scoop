package exceptions;

public class TSNELabelsFileNotFoundException extends TSNEPlotException {

    public TSNELabelsFileNotFoundException() {
        message = "Could not find t-SNE labels file";
    }

}
