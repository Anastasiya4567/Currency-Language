package parser.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class ReturnExpression {
    private Object returnValue;
    private TokenPosition tokenPosition;
}
