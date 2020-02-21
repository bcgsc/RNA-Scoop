package exceptions;

public class GTFInvalidEndNucleotideException extends GTFInvalidNucleotideException {
    public GTFInvalidEndNucleotideException(int lineNumber) {
        super(false, lineNumber);
    }
}
