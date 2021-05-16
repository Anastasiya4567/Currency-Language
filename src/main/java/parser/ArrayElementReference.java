package parser;

import parser.basic.Identifier;
import parser.basic.Number;
import scanner.TokenPosition;

public class ArrayElementReference {
    private Identifier arrayName;
    private Object index;
    private TokenPosition tokenPosition;

    public ArrayElementReference(Identifier arrayName, Identifier index, TokenPosition tokenPosition) {
        this.arrayName = arrayName;
        this.index = index;
        this.tokenPosition = tokenPosition;
    }

    public ArrayElementReference(Identifier arrayName, Number index, TokenPosition tokenPosition) {
        this.arrayName = arrayName;
        this.index = index;
        this.tokenPosition = tokenPosition;
    }
}