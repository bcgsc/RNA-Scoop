package exceptions;

public class MatrixSizeZeroException extends RNAScoopException {

    public MatrixSizeZeroException() {
        message = "Given matrix has a size of 0x0 (smallest size allowed: 1x1)";
    }

}
