package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.basic.Identifier;
import parser.type.Type;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class ArgumentDeclaration {
    private Type type;
    private Identifier identifier;
    private ArgumentValue value;
    private TokenPosition tokenPosition;
}
