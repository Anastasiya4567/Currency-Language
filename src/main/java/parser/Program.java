package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.basic.Import;
import scanner.token.TokenPosition;

import java.util.List;

@Getter
@AllArgsConstructor
public class Program {
    private List<Import> imports;
    private List<FunctionDeclaration> functionDeclaration;
    private TokenPosition tokenPosition;
}
