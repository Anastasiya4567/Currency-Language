package scanner.token;

import lombok.Getter;

@Getter
public class Token {
    private TokenType tokenType;
    private String value;
    private TokenPosition tokenPosition;

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
