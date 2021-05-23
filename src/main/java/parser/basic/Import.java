package parser.basic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

@Getter
@AllArgsConstructor
public class Import {
    private String fileName;
    private TokenPosition tokenPosition;
}
