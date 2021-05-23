package parser.basic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class BigDecimal {
    private java.math.BigDecimal value;
    private TokenPosition position;
}
