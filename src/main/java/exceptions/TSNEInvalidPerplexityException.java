package exceptions;

public class TSNEInvalidPerplexityException extends RNAScoopException {

    public TSNEInvalidPerplexityException() {
        message = "Perplexity field input must be a non-negative number";
    }

}
