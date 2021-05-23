package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class VariableValue extends ArgumentValue {
    private Object value;
    private TokenPosition tokenPosition;
}
