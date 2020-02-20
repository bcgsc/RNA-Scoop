package exceptions;

public class GTFFileMissingColumnsException extends RNAScoopException{
    public GTFFileMissingColumnsException(int lineNumber) {
        message = "The line " + lineNumber + " in the given GTF file has less than 9 columns";
    }
}
