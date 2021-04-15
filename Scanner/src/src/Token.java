package Scanner.src;

public class Token {
    private TokenType tokenType;
    private String value;

    public String getValue() {
        return value;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    Token (TokenType tokenType, String value) {
        this.tokenType = tokenType;
        this.value = value;
    }

}
