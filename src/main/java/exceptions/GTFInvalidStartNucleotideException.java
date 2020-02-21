package exceptions;

public class GTFInvalidStartNucleotideException extends GTFInvalidNucleotideException {
    public GTFInvalidStartNucleotideException(int lineNumber) {
        super(true, lineNumber);
    }
}
