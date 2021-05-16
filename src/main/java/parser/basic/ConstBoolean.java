package parser.basic;

import scanner.TokenPosition;

public class ConstBoolean {
    private String value;

    public String getValue() {
        return value;
    }

    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    private TokenPosition tokenPosition;

    public ConstBoolean(String value, TokenPosition tokenPosition) {
        this.value = value;
        this.tokenPosition = tokenPosition;
    }
}
