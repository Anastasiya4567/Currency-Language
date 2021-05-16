package parser.expression;

import parser.expression.SimpleExpression;
import scanner.TokenPosition;

public class ComplexExpression {
    private SimpleExpression leftSimpleExpression;
    private String relationalOperator;
    private SimpleExpression rightSimpleExpression;
    private TokenPosition tokenPosition;

    public SimpleExpression getLeftSimpleExpression() {
        return leftSimpleExpression;
    }

    public String getRelationalOperator() {
        return relationalOperator;
    }

    public SimpleExpression getRightSimpleExpression() {
        return rightSimpleExpression;
    }

    public ComplexExpression(SimpleExpression leftSimpleExpression, String relationalOperator, SimpleExpression rightSimpleExpression, TokenPosition tokenPosition) {
        this.leftSimpleExpression = leftSimpleExpression;
        this.relationalOperator = relationalOperator;
        this.rightSimpleExpression = rightSimpleExpression;
        this.tokenPosition = tokenPosition;
    }
}
