package exceptions;

public class DuplicateColumnLabelException extends RNAScoopException {

    public DuplicateColumnLabelException(String label) {
        message = "The label \"" + label + "\" is in the column labels file more than once. All column labels should be unique";
    }

}
