package exceptions;

public class EmbeddingColumnsException extends RNAScoopException {

    public EmbeddingColumnsException(int lineNumber) {
        message = "Line " + lineNumber + " in the embedding does not have 2 columns";
    }

}
