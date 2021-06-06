package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.basic.Identifier;
import scanner.token.TokenPosition;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
public class FunctionDeclaration {
    private Object returnType;
    private Identifier functionName;
    private ArrayList<ArgumentDeclaration> arguments;
    private ArrayList<Instruction> instructions;
    private TokenPosition tokenPosition;
}
