package exceptions;

public class TPMGradientInvalidMaxMinException extends RNAScoopException {

    public TPMGradientInvalidMaxMinException() {
        message = "TPM gradient min and max must be non-negative numbers";
    }

}
