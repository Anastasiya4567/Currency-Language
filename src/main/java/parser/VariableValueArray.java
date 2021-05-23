package parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
public class VariableValueArray {
    private ArrayList<Object> values;
    private TokenPosition tokenPosition;
}
