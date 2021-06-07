package parser.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

import java.util.Optional;

@Getter
public class Term {
    private Factor factor;
    private Optional<Operator> operator;
    private Optional<Term> term;
    private TokenPosition tokenPosition;

    public Term (Factor factor, TokenPosition tokenPosition) {
        this.factor = factor;
        this.tokenPosition = tokenPosition;
    }

    public Term (Factor factor, Optional<Operator> operator, Optional<Term> term, TokenPosition tokenPosition) {
        this.factor = factor;
        this.operator = operator;
        this.term = term;
        this.tokenPosition = tokenPosition;
    }

    public enum Operator {
        MULTIPLY,
        DIVIDE
    }
}
