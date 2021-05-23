package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.basic.Identifier;
import parser.type.Type;
import scanner.token.TokenPosition;

import java.util.ArrayList;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class FunctionDeclaration {
    private Type returnType;
    private Identifier functionName;
    private Optional<ArrayList<ArgumentDeclaration>> arguments;
    private Optional<Body> body;
    private TokenPosition tokenPosition;
}
