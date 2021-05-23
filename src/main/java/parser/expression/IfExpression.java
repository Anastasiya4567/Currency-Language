package parser.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

import java.util.Optional;

@Getter
@AllArgsConstructor
public class IfExpression {
    private Object expression;
    private Optional<IfExpression> ifExpression;
    private TokenPosition tokenPosition;
}
