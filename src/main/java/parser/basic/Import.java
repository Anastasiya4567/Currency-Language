package parser.basic;

import scanner.TokenPosition;

public class Import {
    private String fileName;
    private TokenPosition tokenPosition;

    public String getFileName() {
        return fileName;
    }

    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    public Import(String fileName, TokenPosition tokenPosition) {
        this.fileName = fileName;
        this.tokenPosition = tokenPosition;
    }

}
