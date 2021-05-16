package parser.basic;

import scanner.TokenPosition;

public class Number {
    public int getValue() {
        return value;
    }

    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    private int value;
    private TokenPosition tokenPosition;

    public Number(int value, TokenPosition tokenPosition) {
        this.value = value;
        this.tokenPosition = tokenPosition;
    }

}
