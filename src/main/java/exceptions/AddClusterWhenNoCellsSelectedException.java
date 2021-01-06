package exceptions;

public class AddClusterWhenNoCellsSelectedException extends RNAScoopException {

    public AddClusterWhenNoCellsSelectedException() {
        message = "Could not create cluster as no cells are selected";
    }

}
