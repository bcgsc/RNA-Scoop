package exceptions;

public class EmbeddingLengthException extends RNAScoopException {

    public EmbeddingLengthException() {
        message = "The number of rows in the embedding does not equal the number of rows in the expression matrix";
    }

}
