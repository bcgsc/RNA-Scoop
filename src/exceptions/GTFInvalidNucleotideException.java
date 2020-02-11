package exceptions;

public abstract class GTFInvalidNucleotideException extends RNAScoopException {

    public GTFInvalidNucleotideException(boolean isStartNucleotide, int lineNumber) {
        message = "GTF file has invalid " + (isStartNucleotide ? "start" : "end") +
                  " coordinate at line: " + lineNumber + ". " +
                  "The end coordinate must be an integer value > 0";
    }
}
