package parser.expression;

import scanner.TokenPosition;

public class Expression {
    private Object value;
    private TokenPosition tokenPosition;

    public Object getValue() {
        return value;
    }

    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    public Expression(Object identifier, TokenPosition tokenPosition) {
        this.value = identifier;
        this.tokenPosition = tokenPosition;
    }
}
