package parser.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

import java.util.Optional;

@Getter
@AllArgsConstructor
public class CurrencyConversion {
    private CurrencyAbbreviation currencyAbbreviation;
    private Object content;
    private Optional<CurrencyAbbreviation> currency;
    private TokenPosition tokenPosition;
}
