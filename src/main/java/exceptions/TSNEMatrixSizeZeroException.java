package exceptions;

public class TSNEMatrixSizeZeroException extends RNAScoopException {

    public TSNEMatrixSizeZeroException() {
        message = "Given matrix has a size of 0x0 (smallest size allowed: 1x1). Please make sure all columns in matrix are" +
                  " separated by tabs";
    }

}
