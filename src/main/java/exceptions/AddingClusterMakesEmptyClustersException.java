package exceptions;

public class AddingClusterMakesEmptyClustersException extends RNAScoopException {

    public AddingClusterMakesEmptyClustersException(String emptyClusterName) {
        message = "Creating cluster would result in the " + emptyClusterName + " cluster having no cells";
    }

}
