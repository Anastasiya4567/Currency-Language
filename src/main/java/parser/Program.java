package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class Program {
    private Object importOrFunctionDeclaration;
    private MainFunction mainFunction;
    private TokenPosition tokenPosition;
}
