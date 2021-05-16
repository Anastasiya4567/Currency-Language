package parser.basic;

import scanner.TokenPosition;

import java.util.Objects;

public class CurrencyAbbreviation {
    public String getAbbreviation() {
        return abbreviation;
    }

    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    private String abbreviation;
    private TokenPosition tokenPosition;

    public CurrencyAbbreviation(String abbreviation, TokenPosition tokenPosition) {
        this.abbreviation = abbreviation;
        this.tokenPosition = tokenPosition;
    }
}
