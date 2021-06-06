package parser.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.expression.Expression;
import scanner.token.TokenPosition;

import java.util.Optional;

@Getter
@AllArgsConstructor
public class CurrencyConversion {
    private CurrencyAbbreviation currencyAbbreviation;
    private Expression expression;
    private TokenPosition tokenPosition;
}
