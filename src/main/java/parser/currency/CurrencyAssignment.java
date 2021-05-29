package parser.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.currency.CurrencyAbbreviation;
import scanner.token.TokenPosition;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CurrencyAssignment {
    private Object content;
    private CurrencyAbbreviation currencyAbbreviation;
    private TokenPosition tokenPosition;
}
