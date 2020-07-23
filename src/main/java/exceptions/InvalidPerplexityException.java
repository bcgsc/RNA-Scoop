package exceptions;

public class InvalidPerplexityException extends RNAScoopException {

    public InvalidPerplexityException() {
        message = "Perplexity field input must be a non-negative number";
    }

}
