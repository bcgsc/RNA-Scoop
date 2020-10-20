package exceptions;

public class InvalidTPMCutOffException extends RNAScoopException {

    public InvalidTPMCutOffException() {
        message = "TPM cutoffs must be non-negative numbers";
    }

}
