package parser;

import parser.expression.Expression;
import scanner.TokenPosition;

public class VariableDefinition {

    private Object identifier;
    private Object value;
    private TokenPosition tokenPosition;

    public Object getIdentifier() {
        return identifier;
    }

    public Object getValue() {
        return value;
    }

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
