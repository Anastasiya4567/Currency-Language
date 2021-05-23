package scanner;

import org.junit.jupiter.api.Test;
import scanner.token.Token;
import scanner.token.TokenType;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ScannerTest {

    public void scan(StringReader stringReader, ArrayList<Token> tokens) throws Exception {
        Scanner scanner = new Scanner(stringReader);

        while(scanner.getCurrentToken().getTokenType() != TokenType.END_OF_FILE) {
            tokens.add(new Token(scanner.getCurrentToken().getTokenType(), scanner.getCurrentToken().getValue()));
            scanner.next();
        }
    }

    //single symbol token types
    @Test
    public void roundBracketsTest() throws Exception {
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
    public void squareBracketsTest() throws Exception {
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
    public void curlyBracketsTest() throws Exception {
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
    public void assignTest() throws Exception {
        String testString = "int a = 1;";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.ASSIGN,tokens.get(2).getTokenType());
        assertEquals(tokens.get(2).getValue(),"=");
    }

    @Test
    public void commaTest() throws Exception {
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
    public void semicolonTest() throws Exception {
        String testString = " a=b; ";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.SEMI_COLON,tokens.get(tokens.size()-1).getTokenType());
        assertEquals(tokens.get(tokens.size()-1).getValue(),";");
    }

    @Test
    public void pointTest() throws Exception {
        String testString = "currencies.json";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(tokens.size(),3);
        assertEquals(TokenType.POINT,tokens.get(1).getTokenType());
        assertEquals(tokens.get(1).getValue(),".");
    }

    @Test
    public void mathOperationsTest() throws Exception {
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

    // multi symbol token types
    @Test
    public void compareOperationsTest() throws Exception {
        String testString = "2 > 1 && 3 < 19 && 5 >= 1 && 11 <= 32==2!=";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.MORE_THAN,tokens.get(1).getTokenType());
        assertEquals(tokens.get(1).getValue(),">");
        assertEquals(TokenType.LESS_THAN,tokens.get(5).getTokenType());
        assertEquals(tokens.get(5).getValue(),"<");
        assertEquals(TokenType.MORE_OR_EQUALS,tokens.get(9).getTokenType());
        assertEquals(tokens.get(9).getValue(),">=");
        assertEquals(TokenType.LESS_OR_EQUALS,tokens.get(13).getTokenType());
        assertEquals(tokens.get(13).getValue(),"<=");
        assertEquals(TokenType.EQUALS,tokens.get(15).getTokenType());
        assertEquals(tokens.get(15).getValue(),"==");
        assertEquals(TokenType.NOT_EQUALS,tokens.get(17).getTokenType());
        assertEquals(tokens.get(17).getValue(),"!=");

    }

    @Test
    public void andTest() throws Exception {
        String testString = "a && d";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.AND, tokens.get(1).getTokenType());
        assertEquals(tokens.get(1).getValue(),"&&");
    }

    @Test
    public void andTestException() {
        String testString = "a &( d";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();

        Exception exception = assertThrows(Exception.class, () -> {
            scan(stringReader, tokens);
        });

        String expectedMessage = "Expected && but got &( ";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void orTest() throws Exception {
        String testString = "k || m";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.OR, tokens.get(1).getTokenType());
        assertEquals(tokens.get(1).getValue(),"||");
    }

    @Test
    public void orTestException() {
        String testString = "a | d";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();

        Exception exception = assertThrows(Exception.class, () -> {
            scan(stringReader, tokens);
        });

        String expectedMessage = "Expected || but got | ";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void identifierTest() throws Exception {
        String testString = "var_1, 5a_bc";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"var_1");
        assertNotEquals(TokenType.IDENTIFIER, tokens.get(2).getTokenType());
    }

    @Test
    public void identifierLengthExceptionTest() {
        StringBuilder testString = new StringBuilder();
        testString.append("aB".repeat(150));
        StringReader stringReader = new StringReader(testString.toString());
        ArrayList<Token> tokens = new ArrayList<>();

        Exception exception = assertThrows(Exception.class, () -> {
            scan(stringReader, tokens);
        });

        String expectedMessage = "The length of identifier can't be so large";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void numberTest() throws Exception {
        String testString = "52182;";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.NUMBER, tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"52182");
    }

    @Test
    public void bigDecimalTest() throws Exception {
        String testString = "BigDecimal a = 213.231, 0.11";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.BIG_DECIMAL_NUMBER, tokens.get(3).getTokenType());
        assertEquals(tokens.get(3).getValue(),"213.231");
        assertEquals(TokenType.BIG_DECIMAL_NUMBER, tokens.get(5).getTokenType());
        assertEquals(tokens.get(5).getValue(),"0.11");

    }

    @Test
    public void bigDecimalExceptionTest() {
        String[] testString = {"01.12", "032", "5.."};

        for (int i=0; i<3; i++){
            StringReader stringReader = new StringReader(testString[i]);
            ArrayList<Token> tokens = new ArrayList<>();

            Exception exception = assertThrows(Exception.class, () -> {
                scan(stringReader, tokens);
            });

            String expectedMessage = "Bad format of number";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));

        }
    }

    @Test
    public void numberLengthExceptionTest() {
        String testString = "254534364646756775674593583957857834573485734323";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();

        Exception exception = assertThrows(Exception.class, () -> {
            scan(stringReader, tokens);
        });

        String expectedMessage = "The length of number can't be so large";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void commentTest() throws Exception {
        String testString = "/* c = a*b/c */";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.COMMENT, tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"/* c = a*b/c */");
    }

    @Test
    public void commentExceptionTest() {
        String testString = "/*  this is a comment";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();

        Exception exception = assertThrows(Exception.class, () -> {
            scan(stringReader, tokens);
        });

        String expectedMessage = "Unclosed comment";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void spacesNumberExceptionTest() {
        StringBuilder spaces = new StringBuilder();
        spaces.append(" ".repeat(1200));

        StringReader stringReader = new StringReader(spaces.toString());
        ArrayList<Token> tokens = new ArrayList<>();

        Exception exception = assertThrows(Exception.class, () -> {
            scan(stringReader, tokens);
        });

        String expectedMessage = "The number of spaces can't be so large";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void constStringTest() throws Exception {
        String testString = "\"I do it\"";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(TokenType.CONST_STRING, tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"\"I do it\"");
    }

    @Test
    public void constStringExceptionTest() {
        String testString = "\"this is a string literal";

        StringReader stringReader = new StringReader(testString.toString());
        ArrayList<Token> tokens = new ArrayList<>();

        Exception exception = assertThrows(Exception.class, () -> {
            scan(stringReader, tokens);
        });

        String expectedMessage = "Unclosed string literal";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));

    }

    @Test
    public void constStringLengthExceptionTest() {
        StringBuilder testString = new StringBuilder("\"");
        testString.append("qf4".repeat(3500));
        testString.append("\"");

        StringReader stringReader = new StringReader(testString.toString());
        ArrayList<Token> tokens = new ArrayList<>();

        Exception exception = assertThrows(Exception.class, () -> {
            scan(stringReader, tokens);
        });

        String expectedMessage = "The length of quotation can't be so large";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    //undefined token type
    @Test
    public void undefinedTest() throws Exception {
        String testString = "@#1";
        StringReader stringReader = new StringReader(testString);
        ArrayList<Token> tokens = new ArrayList<>();
        scan(stringReader, tokens);

        assertEquals(tokens.size(),3);
        assertEquals(TokenType.UNDEFINED, tokens.get(0).getTokenType());
        assertEquals(tokens.get(0).getValue(),"");
    }

}
