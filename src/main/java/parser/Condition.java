package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.expression.IfExpression;
import scanner.token.TokenPosition;

import java.util.Optional;

@Getter
@AllArgsConstructor
public class Condition {
    private boolean isNegated;
    private IfExpression ifExpression;
    private Optional<Condition> condition;
    private TokenPosition tokenPosition;
}
