package parser.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class SimpleExpression {
    private boolean isNegated;
    private Object content;
    private TokenPosition tokenPosition;
}
