package parser;

import parser.basic.Identifier;
import scanner.TokenPosition;

import java.util.ArrayList;

public class FunctionCall {
    private Identifier name;
    private ArrayList<Object> arguments;
    private TokenPosition tokenPosition;

    public Identifier getName() {
        return name;
    }

    public ArrayList<Object> getArguments() {
        return arguments;
    }

    public FunctionCall(Identifier name, ArrayList<Object> arguments, TokenPosition tokenPosition) {
        this.name = name;
        this.arguments = arguments;
        this.tokenPosition = tokenPosition;
    }
}
