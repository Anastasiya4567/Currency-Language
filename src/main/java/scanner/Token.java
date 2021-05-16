package scanner;

public class Token {
    private TokenType tokenType;
    private String value;

    private TokenPosition tokenPosition;

    public String getValue() {
        return value;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    public Token (TokenType tokenType, String value) {
        this.tokenType = tokenType;
        this.value = value;
        this.tokenPosition = new TokenPosition();
    }

    public Token (TokenType tokenType, String value, TokenPosition tokenPosition) {
        this.tokenType = tokenType;
        this.value = value;
        this.tokenPosition = tokenPosition;
    }

}
