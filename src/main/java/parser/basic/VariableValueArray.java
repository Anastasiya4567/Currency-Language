package parser.basic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scanner.token.TokenPosition;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
public class VariableValueArray {
    private ArrayList<VariableValue> values;
    private TokenPosition tokenPosition;
}
