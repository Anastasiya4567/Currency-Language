package parser.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class CurrencyAbbreviation {
    private String abbreviation;
    private TokenPosition tokenPosition;
}
