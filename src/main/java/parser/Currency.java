package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.currency.CurrencyAbbreviation;
import scanner.token.TokenPosition;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class Currency {
    private BigDecimal value;
    private CurrencyAbbreviation currencyAbbreviation;
    private TokenPosition tokenPosition;
}
