package exceptions;

public class InvalidPercentCutOff extends RNAScoopException {
    public InvalidPercentCutOff() {
        this.message = "Percent cutoffs must be numbers in range [0, 100]";
    }
}
