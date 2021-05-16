package parser;

import parser.basic.CurrencyAbbreviation;
import scanner.TokenPosition;

import java.math.BigDecimal;

public class Currency {
    private BigDecimal value;
    private CurrencyAbbreviation currencyAbbreviation;
    private TokenPosition tokenPosition;

    public BigDecimal getValue() {
        return value;
    }

    public CurrencyAbbreviation getCurrencyAbbreviation() {
        return currencyAbbreviation;
    }

    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    public Currency(BigDecimal value, CurrencyAbbreviation currencyAbbreviation, TokenPosition tokenPosition) {
        this.value = value;
        this.currencyAbbreviation = currencyAbbreviation;
        this.tokenPosition = tokenPosition;
    }



}
