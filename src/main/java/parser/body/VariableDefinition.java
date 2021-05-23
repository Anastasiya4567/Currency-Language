package parser.body;

import lombok.Getter;
import parser.expression.Expression;
import scanner.token.TokenPosition;

@Getter
public class VariableDefinition {

    private Object identifier;
    private Object value;
    private TokenPosition tokenPosition;

    public VariableDefinition(Object identifier, Object value, TokenPosition tokenPosition) {
        this.identifier = identifier;
        this.value = value;
        this.tokenPosition = tokenPosition;
    }

    public VariableDefinition(Object identifier, Expression expression, TokenPosition tokenPosition) {
        this.identifier = identifier;
        this.value = expression;
        this.tokenPosition = tokenPosition;
    }



}
