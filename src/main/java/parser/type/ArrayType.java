package parser.type;

import parser.type.SimpleType;
import scanner.TokenPosition;

public class ArrayType  extends Type {
    private SimpleType type;
    private Integer size;

    public SimpleType getType() {
        return type;
    }

    public Integer getSize() {
        return size;
    }

    public ArrayType(SimpleType type, Integer size, TokenPosition tokenPosition) {
        super(tokenPosition);
        this.type = type;
        this.size = size;
    }
}
