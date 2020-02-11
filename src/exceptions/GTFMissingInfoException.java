package exceptions;

public class GTFMissingInfoException extends RNAScoopException{
    public GTFMissingInfoException(int lineNumber) {
        message = "Attributes column on line: " + lineNumber + "is missing required information." +
                  "Required information includes transcript_id and gene_id";
    }
}
