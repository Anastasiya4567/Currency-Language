package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class Instruction {
    private Object body;
    private TokenPosition tokenPosition;
}
