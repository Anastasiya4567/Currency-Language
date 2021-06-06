package parser.instruction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.basic.Identifier;
import parser.type.Type;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class VariableDeclaration {
    private Type type;
    private Identifier identifier;
    private Object value;
    private TokenPosition tokenPosition;
}
