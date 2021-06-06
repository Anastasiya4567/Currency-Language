package parser.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.instruction.VariableDeclaration;
import parser.instruction.VariableDefinition;

@Getter
@AllArgsConstructor
public class ForExpression {
    private VariableDeclaration variableDeclaration;
    private Object complexExpression;
    private VariableDefinition variableDefinition;
}
