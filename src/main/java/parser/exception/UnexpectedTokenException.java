package parser.exception;

import scanner.token.Token;

public class UnexpectedTokenException extends Exception {
    public UnexpectedTokenException(String expectedToken, Token token) {
        super("Expected '" + expectedToken + "' but got '" + token.getValue() +
                "' at line " + token.getTokenPosition().getLine() + ", at column " + token.getTokenPosition().getColumn());
    }
}
