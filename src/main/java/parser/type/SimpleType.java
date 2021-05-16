package parser.type;

import scanner.TokenPosition;

public class SimpleType extends Type {
    private String type;

    public String getType() {
        return type;
    }


    public SimpleType(String type, TokenPosition tokenPosition) {
        super(tokenPosition);
        this.type = type;
    }
}