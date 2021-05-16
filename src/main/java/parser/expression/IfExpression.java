package parser.expression;

import scanner.TokenPosition;

import java.util.Optional;

public class IfExpression {
    private Object expression;
    private Optional<IfExpression> ifExpression;
    private TokenPosition tokenPosition;

    public IfExpression(Object expression, Optional<IfExpression> ifExpression, TokenPosition tokenPosition) {
        this.expression = expression;
        this.ifExpression = ifExpression;
        this.tokenPosition = tokenPosition;
    }
}
