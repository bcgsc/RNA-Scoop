package exceptions;

public class InvalidMinFoldChangeCutOff extends RNAScoopException{
    public InvalidMinFoldChangeCutOff() {
        this.message = "Min fold change cutoffs must be numbers ≥ 1";
    }
}
