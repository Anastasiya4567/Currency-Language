package parser.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.currency.CurrencyAbbreviation;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class CurrencyAssignment {
    private Object content;
    private CurrencyAbbreviation currencyAbbreviation;
    private TokenPosition tokenPosition;
}
