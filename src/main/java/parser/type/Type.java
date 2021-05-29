package parser.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public abstract class Type {
    private TokenPosition tokenPosition;
}
