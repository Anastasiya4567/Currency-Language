package parser.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.body.VariableDeclaration;

@Getter
@AllArgsConstructor
public class ForExpression {
    private VariableDeclaration variableDeclaration;
    private Object complexExpression;
    private Expression expression;
}
