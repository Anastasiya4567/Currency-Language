package scanner;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScannerTest {

    public void scan(StringReader stringReader, ArrayList<TokenType> tokens) {
        Scanner scanner = new Scanner(stringReader);

        while(scanner.getCurrentToken().getTokenType() != TokenType.END_OF_FILE) {
            tokens.add(scanner.getCurrentToken().getTokenType());
            scanner.next();
        }
    }

    @Test
    public void roundBracketsTest() {
        String testString = "((   )   ) ";
        StringReader stringReader = new StringReader(testString);
        ArrayList<TokenType> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.OPEN_ROUND_BRACKET,tokens.get(0));
        assertEquals(TokenType.OPEN_ROUND_BRACKET,tokens.get(1));
        assertEquals(TokenType.CLOSE_ROUND_BRACKET,tokens.get(2));
        assertEquals(TokenType.CLOSE_ROUND_BRACKET,tokens.get(3));
    }

    @Test
    public void squareBracketsTest() {
        String testString = "    [i]";
        StringReader stringReader = new StringReader(testString);
        ArrayList<TokenType> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.OPEN_SQUARE_BRACKET,tokens.get(0));
        assertEquals(TokenType.CLOSE_SQUARE_BRACKET,tokens.get(2));
    }

    @Test
    public void curlyBracketsTest() {
        String testString = "{1USD, 2USD  }  ";
        StringReader stringReader = new StringReader(testString);
        ArrayList<TokenType> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.OPEN_CURLY_BRACKET,tokens.get(0));
        assertEquals(TokenType.CLOSE_CURLY_BRACKET,tokens.get(tokens.size() - 1));
    }

    @Test
    public void isTest() {
        String testString = "int a = 1;";
        StringReader stringReader = new StringReader(testString);
        ArrayList<TokenType> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.IS,tokens.get(2));
    }

    @Test
    public void commaTest() {
        String testString = "a,  b,c,d ,e";
        StringReader stringReader = new StringReader(testString);
        ArrayList<TokenType> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        int numberOfCommas = 0;
        for (TokenType token: tokens) {
            if (token == TokenType.COMMA)
                numberOfCommas++;
        }

        assertEquals(TokenType.COMMA, tokens.get(1));
        assertEquals(TokenType.COMMA, tokens.get(3));
        assertEquals(TokenType.COMMA, tokens.get(7));
        assertEquals(numberOfCommas,4);
    }
}
