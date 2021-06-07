package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
public class MainFunction {
    private ArrayList<Instruction> body;
    private TokenPosition tokenPosition;
}
