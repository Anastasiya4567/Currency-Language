package parser.type;

import scanner.TokenPosition;

public abstract class Type {
//    private Object type;
    private TokenPosition tokenPosition;

//    public Object getType() {
//        return type;
//    }

    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }
    public Type(TokenPosition tokenPosition) {
        this.tokenPosition = tokenPosition;
    }

//    public Type(SimpleType simpleType, TokenPosition tokenPosition) {
//        this.type = simpleType;
//        this.tokenPosition = tokenPosition;
//    }
//
//    public Type(ArrayType arrayType, TokenPosition tokenPosition) {
//        this.type = arrayType;
//        this.tokenPosition = tokenPosition;
//    }
}
