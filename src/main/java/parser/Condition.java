package parser;

import parser.expression.IfExpression;
import scanner.TokenPosition;

import java.util.Optional;


public class Condition {
    private boolean isNegated;
    private IfExpression ifExpression;
    private Optional<Condition> condition;
    private TokenPosition tokenPosition;

    public Condition(boolean isNegated, IfExpression ifExpression, Optional<Condition> condition, TokenPosition tokenPosition) {
        this.isNegated = isNegated;
        this.ifExpression = ifExpression;
        this.condition = condition;
        this.tokenPosition = tokenPosition;
    }
}
