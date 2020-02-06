package exceptions;

public class InvalidPerplexityException extends TSNEPlotException {

    public InvalidPerplexityException() {
        message = "Perplexity field input must be a non-negative number";
    }

}
