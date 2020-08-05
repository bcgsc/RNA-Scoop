package exceptions;

public class InvalidPerplexityException extends RNAScoopException {

    public InvalidPerplexityException() {
        message = "Perplexity must be a non-negative number";
    }

}
