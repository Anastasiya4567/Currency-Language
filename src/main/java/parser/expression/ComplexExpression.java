package parser.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class ComplexExpression {
    private SimpleExpression leftSimpleExpression;
    private String relationalOperator;
    private SimpleExpression rightSimpleExpression;
    private TokenPosition tokenPosition;
}
