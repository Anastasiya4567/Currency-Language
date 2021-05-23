package parser.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
public class SimpleType extends Type {
    private String type;

    public SimpleType(String type, TokenPosition tokenPosition) {
        super(tokenPosition);
        this.type = type;
    }
}