package exceptions;

public class GTFMissingAttributesInfoException extends RNAScoopException{
    public GTFMissingAttributesInfoException(int lineNumber) {
        message = "Attributes column on line: " + lineNumber + " is missing required information." +
                  "Required information includes transcript_id and gene_id";
    }
}
