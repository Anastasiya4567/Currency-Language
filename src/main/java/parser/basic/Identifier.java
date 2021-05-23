package parser.basic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class Identifier {
    private String name;
    private TokenPosition tokenPosition;
}
