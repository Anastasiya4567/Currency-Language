package Scanner.src;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Scanner {
    private FileReader fileReader;
    private Token currentToken;
    private char character;

    public Token getCurrentToken() {
        return currentToken;
    }

    public Scanner (FileReader fileReader) {
        this.fileReader = fileReader;
        next();
    }
    private void addToken(ArrayList<Token> tokens, TokenType type, String value) {
        currentToken = new Token(type, value);
    }

    public void next() {
        ArrayList<TokenType> tokens = new ArrayList<TokenType>();
        try {
            int i;
            i = fileReader.read();
            if (!Character.isWhitespace((char)i)) {

            }

            if (i==-1) {
                this.currentToken = new Token(TokenType.END_OF_FILE, "-1");
            }


            if ((i=fileReader.read()) != -1 || !Character.isWhitespace((char)i)) {
                char character = (char) i;
//                if (character=>'a' && character <= 'z' || i >= 'A' && i <= 'Z' || i == '_') {
////                    while();
////                    currentToken = new Scanner.Scanner.Token(value, type);
//
//                }

                switch ((char)i) {
                    case ' ':
                        break;
                    case '{':
                        tokens.add(TokenType.OPEN_CURLY_BRACKET);
                        break;
                    case '}':
                        tokens.add(TokenType.CLOSE_CURLY_BRACKET);
                        break;
                    case '(':
                        tokens.add(TokenType.OPEN_ROUND_BRACKET);
                        break;
                    case ')':
                        tokens.add(TokenType.CLOSE_ROUND_BRACKET);
                        break;
                    case '[':
                        tokeType = TokenType.OPEN_SQUARE_BRACKET;
                        break;
                    case ']':
                        tokens.add(TokenType.CLOSE_SQUARE_BRACKET);
                        break;
                    case '=':
                        tokens.add(TokenType.EQUALS);
                        break;
                    case ',':
                        tokens.add(TokenType.COMMA);
                        break;
                    case ';':
                        tokens.add(TokenType.SEMI_COLON);
                        break;
                    case '!':
                        addToken (tokens, TokenType.EXCLAMATION_MARK, "!");
//                        Scanner.Scanner.Token currentToken = new Scanner.Scanner.Token(Scanner.TokenType.EXCLAMATION_MARK, "!");
//                        tokens.add(currentToken);
                        break;
                    case '"':
                        Token currentToken = new Token(TokenType.QUATATION_MARKS, "\"");
                        tokens.add(currentToken);
                        break;

                }
                System.out.println((char)i);

            }
            System.out.println(tokens);
        } catch (IOException e) {

        }
//        aa(fileReader);

    }
}
