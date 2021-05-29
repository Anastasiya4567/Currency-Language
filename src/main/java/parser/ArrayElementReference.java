package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.basic.Identifier;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class ArrayElementReference {
    private Identifier arrayName;
    private Object index;
    private TokenPosition tokenPosition;
}