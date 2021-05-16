package parser.basic;

import scanner.TokenPosition;

public class Identifier {
    private String name;
    private TokenPosition tokenPosition;

    public String getName() {
        return name;
    }

    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    public Identifier (String name, TokenPosition tokenPosition) {
        this.name = name;
        this.tokenPosition = tokenPosition;
    }
}
