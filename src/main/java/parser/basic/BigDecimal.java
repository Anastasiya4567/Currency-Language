package parser.basic;

import scanner.TokenPosition;

public class BigDecimal {
    private java.math.BigDecimal value;
    private TokenPosition position;

    public java.math.BigDecimal getValue() {
        return value;
    }

    public TokenPosition getPos() {
        return position;
    }

    public BigDecimal(java.math.BigDecimal value, TokenPosition position) {
        this.value = value;
        this.position = position;
    }
}
