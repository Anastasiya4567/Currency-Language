package parser.body;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.Body;
import parser.basic.Identifier;
import parser.expression.ForExpression;

//ForStatement = ‘for’, ‘(‘, ForExpression, ‘)’ ‘{‘, { Body }, ‘}’
@Getter
@AllArgsConstructor
public class ForStatement {
    private ForExpression forExpression;
    private Body body;
}
