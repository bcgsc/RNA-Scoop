package exceptions;

public class GTFAttributeFormatException extends RNAScoopException {
    public GTFAttributeFormatException(int lineNumber) {
        message = "The attributes section on line " + lineNumber + " of the given GTF file has a formatting error.";
    }
}
