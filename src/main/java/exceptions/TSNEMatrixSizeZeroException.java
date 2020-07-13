package exceptions;

public class TSNEMatrixSizeZeroException extends RNAScoopException {

    public TSNEMatrixSizeZeroException() {
        message = "Given matrix has a size of 0x0 (smallest size allowed: 1x1)";
    }

}
