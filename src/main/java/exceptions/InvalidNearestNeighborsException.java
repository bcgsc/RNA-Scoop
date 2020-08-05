package exceptions;

public class InvalidNearestNeighborsException extends RNAScoopException {

    public InvalidNearestNeighborsException() {
        message = "Number of nearest neighbors must be an integer greater or equal to 2";
    }

}
