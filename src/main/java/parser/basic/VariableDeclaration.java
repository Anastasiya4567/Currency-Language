package parser.basic;

import parser.basic.Identifier;
import parser.type.Type;
import scanner.TokenPosition;

public class VariableDeclaration {
    private Type type;
    private Identifier identifier;
    private Object value;
    private TokenPosition tokenPosition;

    public Type getType() {
        return type;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Object getValue() {
        return value;
    }

    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    public VariableDeclaration(Type type, Identifier identifier, Object value, TokenPosition tokenPosition) {
        this.type = type;
        this.identifier = identifier;
        this.value = value;
        this.tokenPosition = tokenPosition;
    }
}
