package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.expression.ReturnExpression;
import scanner.token.TokenPosition;

import java.util.ArrayList;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class MainFunction {
    private ArrayList<Instruction> body;
    private TokenPosition tokenPosition;
}
