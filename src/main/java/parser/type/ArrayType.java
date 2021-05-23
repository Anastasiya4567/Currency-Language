package parser.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
public class ArrayType  extends Type {
    private SimpleType type;
    private Integer size;

    public ArrayType(SimpleType type, Integer size, TokenPosition tokenPosition) {
        super(tokenPosition);
        this.type = type;
        this.size = size;
    }
}
