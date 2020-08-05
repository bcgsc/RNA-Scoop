package exceptions;

public class EmbeddingNotNumberException extends RNAScoopException {

    public EmbeddingNotNumberException(String coordinates, int lineNumber) {
        message = "The coordinates " + coordinates + " at line " + lineNumber + " in the embedding contain non-numerical values";
    }

}
