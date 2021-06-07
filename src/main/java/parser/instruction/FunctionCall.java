package parser.instruction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.basic.Identifier;
import scanner.token.TokenPosition;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
public class FunctionCall {
    private Identifier name;
    private ArrayList<Object> arguments;
    private TokenPosition tokenPosition;
}
