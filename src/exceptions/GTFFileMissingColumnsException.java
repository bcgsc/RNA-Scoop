package exceptions;

public class GTFFileMissingColumnsException extends RNAScoopException{
    public GTFFileMissingColumnsException() {
        message = "Given GTF file has less than 9 columns";
    }
}
