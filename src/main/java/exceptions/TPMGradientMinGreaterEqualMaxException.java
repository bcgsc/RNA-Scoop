package exceptions;

public class TPMGradientMinGreaterEqualMaxException extends RNAScoopException {

    public TPMGradientMinGreaterEqualMaxException() {
        message = "TPM gradient min must be less than the TPM gradient max";
    }

}