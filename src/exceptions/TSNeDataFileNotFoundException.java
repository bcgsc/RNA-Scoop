package exceptions;

public class TSNeDataFileNotFoundException extends TSNEPlotException {

    public TSNeDataFileNotFoundException() {
        message = "Could not find t-SNE data file";
    }

}
