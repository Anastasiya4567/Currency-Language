package parser.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public abstract class Type {
    private TokenPosition tokenPosition;

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
