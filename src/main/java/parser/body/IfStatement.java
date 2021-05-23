package parser.body;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.Body;
import parser.Condition;

import java.util.Optional;

//IfStatement = ‘if’, ‘(‘, Condition, ‘)’, ‘{‘, { Body }, ‘}’, [‘else’, (ifStatement  | ‘{‘, { Body }, ‘}’)];
@Getter
@AllArgsConstructor
public class IfStatement {
    private Condition condition;
    private Optional<Body> ifBody;
    private Optional<Body> elseBody;
    private Optional<IfStatement> ifStatement;
}
