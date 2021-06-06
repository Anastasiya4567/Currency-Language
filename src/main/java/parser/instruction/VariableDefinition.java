package parser.instruction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class VariableDefinition {
    private Object identifier;
    private Object value;
    private TokenPosition tokenPosition;
}
