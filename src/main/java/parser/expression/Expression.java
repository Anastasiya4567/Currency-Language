package parser.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

import java.util.Optional;

@Getter
public class Expression {
    private Term term;
    private Optional<Operator> operator;
    private Optional<Expression> expression;
    private TokenPosition tokenPosition;

    public Expression (Term term, TokenPosition tokenPosition) {
        this.term = term;
        this.tokenPosition = tokenPosition;
    }

    public Expression (Term term, Optional<Operator> operator, Optional<Expression> expression, TokenPosition tokenPosition) {
        this.term = term;
        this.operator = operator;
        this.expression = expression;
        this.tokenPosition = tokenPosition;
    }

    public enum Operator {
        PLUS,
        MINUS
    }
}
