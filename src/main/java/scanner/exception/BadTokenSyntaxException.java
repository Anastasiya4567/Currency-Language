package scanner.exception;

public class BadTokenSyntaxException extends Exception{

    public BadTokenSyntaxException(String error, int line, int column) {
        super(error + " at line " + line + ", at column " + column);
    }
}
