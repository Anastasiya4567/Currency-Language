package scanner;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ScannerTest {

    public void scan(StringReader stringReader, ArrayList<Token> tokens) {
        Scanner scanner = new Scanner(stringReader);

        while(scanner.getCurrentToken().getTokenType() != TokenType.END_OF_FILE) {
            tokens.add(new Token(scanner.getCurrentToken().getTokenType(), scanner.getCurrentToken().getValue()));
            scanner.next();
        }
    }

    //single symbol token types
    @Test
    public void roundBracketsTest() {
        String testString = "((   )   ) ";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.OPEN_ROUND_BRACKET,tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"(");
        assertEquals(TokenType.OPEN_ROUND_BRACKET,tokens.get(1).getTokenType());
        assertEquals(tokens.get(1).getValue(),"(");
        assertEquals(TokenType.CLOSE_ROUND_BRACKET,tokens.get(2).getTokenType());
        assertEquals(tokens.get(2).getValue(),")");
        assertEquals(TokenType.CLOSE_ROUND_BRACKET,tokens.get(3).getTokenType());
        assertEquals(tokens.get(3).getValue(),")");
    }

    @Test
    public void squareBracketsTest() {
        String testString = "    [i]";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.OPEN_SQUARE_BRACKET, tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"[");
        assertEquals(TokenType.CLOSE_SQUARE_BRACKET, tokens.get(2).getTokenType());
        assertEquals(tokens.get(2).getValue(),"]");
    }

    @Test
    public void curlyBracketsTest() {
        String testString = "{1USD, 2USD  }  ";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.OPEN_CURLY_BRACKET, tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"{");
        assertEquals(TokenType.CLOSE_CURLY_BRACKET, tokens.get(tokens.size() - 1).getTokenType());
        assertEquals(tokens.get(tokens.size() - 1).getValue(),"}");
    }

    @Test
    public void isTest() {
        String testString = "int a = 1;";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.IS,tokens.get(2).getTokenType());
        assertEquals(tokens.get(2).getValue(),"=");
    }

    @Test
    public void commaTest() {
        String testString = "a,  b,c,d ,e, ";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        int numberOfCommas = 0;
        for (Token token: tokens) {
            if (token.getTokenType() == TokenType.COMMA)
                numberOfCommas++;
        }

        assertEquals(TokenType.COMMA, tokens.get(1).getTokenType());
        assertEquals(tokens.get(1).getValue(),",");
        assertEquals(TokenType.COMMA, tokens.get(3).getTokenType());
        assertEquals(tokens.get(3).getValue(),",");
        assertEquals(TokenType.COMMA, tokens.get(7).getTokenType());
        assertEquals(tokens.get(7).getValue(),",");
        assertEquals(numberOfCommas,5);
    }

    @Test
    public void semicolonTest() {
        String testString = " a=b; ";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.SEMI_COLON,tokens.get(tokens.size()-1).getTokenType());
        assertEquals(tokens.get(tokens.size()-1).getValue(),";");
    }

    @Test
    public void pointTest() {
        String testString = "currencies.json";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(tokens.size(),3);
        assertEquals(TokenType.POINT,tokens.get(1).getTokenType());
        assertEquals(tokens.get(1).getValue(),".");
    }

    @Test
    public void mathOperationsTest() {
        String testString = "a + b - 1*c/2";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(tokens.size(),9);
        assertEquals(TokenType.PLUS,tokens.get(1).getTokenType());
        assertEquals(tokens.get(1).getValue(),"+");
        assertEquals(TokenType.MINUS,tokens.get(3).getTokenType());
        assertEquals(tokens.get(3).getValue(),"-");
        assertEquals(TokenType.MULTIPLY,tokens.get(5).getTokenType());
        assertEquals(tokens.get(5).getValue(),"*");
        assertEquals(TokenType.DIVIDE,tokens.get(7).getTokenType());
        assertEquals(tokens.get(7).getValue(),"/");
    }

    @Test
    public void compareOperationsTest() {
        String testString = "2 > 1 && 3 < 19";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.MORE_THAN,tokens.get(1).getTokenType());
        assertEquals(tokens.get(1).getValue(),">");
        assertEquals(TokenType.LESS_THAN,tokens.get(5).getTokenType());
        assertEquals(tokens.get(5).getValue(),"<");

    }

    // multi symbol token types
    @Test
    public void andTest() {
        String testString = "a && b &$ d";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.AND, tokens.get(1).getTokenType());
        assertEquals(tokens.get(1).getValue(),"&&");
        assertNotEquals(TokenType.AND, tokens.get(3).getTokenType());
    }

    @Test
    public void orTest() {
        String testString = "k || m |d";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.OR, tokens.get(1).getTokenType());
        assertEquals(tokens.get(1).getValue(),"||");
        assertNotEquals(TokenType.OR, tokens.get(3).getTokenType());
    }

    @Test
    public void identifierTest() {
        String testString = "var_1, 5a_bc";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"var_1");
        assertNotEquals(TokenType.IDENTIFIER, tokens.get(2).getTokenType());
    }

    @Test
    public void numberTest() {
        String testString = "52182;";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.NUMBER, tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"52182");
    }

    @Test
    public void bigIntegerTest() {
        String testString = "BigInteger a = 213.231";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.BIG_INTEGER, tokens.get(tokens.size()-1).getTokenType());
        assertEquals(tokens.get(tokens.size()-1).getValue(),"213.231");
    }

    @Test
    public void commentTest() {
        String testString = "/* c = a*b/c */";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.COMMENT, tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"/* c = a*b/c */");
    }

    @Test
    public void constStringTest() {
        String testString = "\"I do it\"";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.CONST_STRING, tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"\"I do it\"");
    }

    //undefined token type
    @Test
    public void undefinedTest() {
        String testString = "@#1";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(tokens.size(),3);
        assertEquals(TokenType.UNDEFINED, tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"");
    }

}
