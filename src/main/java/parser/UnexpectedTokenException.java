package parser;

import scanner.Token;
import scanner.TokenPosition;

public class UnexpectedTokenException extends Exception {
    public UnexpectedTokenException(String expectedToken, String badToken, TokenPosition tokenPosition) {
        super("Expected '" + expectedToken + "' but got '" + badToken +
                "' at line " + tokenPosition.getLine() + ", at column " + tokenPosition.getColumn());
    }
    public UnexpectedTokenException(String expectedToken, Token token) {
        super("Expected '" + expectedToken + "' but got '" + token.getValue() +
                "' at line " + token.getTokenPosition().getLine() + ", at column " + token.getTokenPosition().getColumn());
    }
}
