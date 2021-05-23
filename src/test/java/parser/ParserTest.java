package parser;

import org.junit.jupiter.api.Test;
import parser.basic.*;
import parser.basic.Number;
import parser.body.VariableDeclaration;
import parser.currency.CurrencyAbbreviation;
import parser.currency.CurrencyAssignment;
import parser.currency.CurrencyConversion;
import parser.expression.Expression;
import parser.type.ArrayType;
import parser.type.SimpleType;
import scanner.IScanner;
import scanner.token.Token;
import scanner.token.TokenPosition;
import scanner.token.TokenType;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
    class ScannerMock implements IScanner {
        private ArrayList<Token> tokens;
        private int index = 0;
        ScannerMock(ArrayList<Token> tokens) {
            this.tokens = tokens;
            this.index = 0;
        }
        @Override
        public Token getCurrentToken() {
            if(index >= tokens.size())
                return new Token(TokenType.END_OF_FILE, "$");
            return tokens.get(index);
        }

        @Override
        public void next() {
            index++;
        }
    }

    @Test
    public void buildNumberTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.NUMBER, "312"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        Number number = parser.tryBuildNumber();
        assertEquals(number.getValue(), 312);
    }

    @Test
    public void buildImportTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IMPORT, "import"));
        tokens.add(new Token(TokenType.CONST_STRING, "currencies.json"));
        tokens.add(new Token(TokenType.SEMI_COLON, ";"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        Import _import = parser.tryBuildImport();
        assertEquals(_import.getFileName(), "currencies.json");
    }

    @Test
    public void buildBigDecimalTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.BIG_DECIMAL_NUMBER, "12.34"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        BigDecimal bigDecimal = parser.tryBuildBigDecimal();
        BigDecimal testBigDecimal = new BigDecimal(
                new java.math.BigDecimal("12.34"), new TokenPosition());
        assertTrue(bigDecimal.getValue().compareTo(testBigDecimal.getValue()) == 0);
    }

    @Test
    public void buildCurrencyAbbreviationTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IDENTIFIER, "USD"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        CurrencyAbbreviation currencyAbbreviation = parser.tryBuildCurrencyAbbreviation();
        CurrencyAbbreviation testCurrencyAbbreviation = new CurrencyAbbreviation("USD", new TokenPosition());
        assertEquals(currencyAbbreviation.getAbbreviation(), testCurrencyAbbreviation.getAbbreviation());
    }

    @Test
    public void buildVariableDeclarationTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IDENTIFIER, "int"));
        tokens.add(new Token(TokenType.IDENTIFIER, "i"));
        tokens.add(new Token(TokenType.ASSIGN, "="));
        tokens.add(new Token(TokenType.NUMBER, "4"));
        tokens.add(new Token(TokenType.SEMI_COLON, ";"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        VariableDeclaration variableDeclaration = parser.tryBuildVariableDeclaration();
        SimpleType simpleType = (SimpleType)variableDeclaration.getType();

        assertEquals(simpleType.getType(), "int");
        Expression expression = (Expression)variableDeclaration.getValue();
        Number number = (Number)expression.getValue();
        assertEquals(variableDeclaration.getIdentifier().getName(), "i");
        assertEquals(number.getValue(), 4);
    }

    @Test
    public void buildArrayTypeTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.OPEN_SQUARE_BRACKET, "["));
        tokens.add(new Token(TokenType.CLOSE_SQUARE_BRACKET, "]"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        ArrayType arrayType = parser.tryBuildArrayType(new SimpleType("String", new TokenPosition()));
        ArrayType testArrayType = new ArrayType(
                new SimpleType("String", new TokenPosition()), null, new TokenPosition());
        assertEquals(arrayType.getSize(), testArrayType.getSize());
    }

    @Test
    public void buildArrayTypeWithSizeTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.OPEN_SQUARE_BRACKET, "["));
        tokens.add(new Token(TokenType.NUMBER, "12"));
        tokens.add(new Token(TokenType.CLOSE_SQUARE_BRACKET, "]"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        ArrayType arrayType = parser.tryBuildArrayType(new SimpleType("int", new TokenPosition()));
        ArrayType testArrayType = new ArrayType(
                new SimpleType("int", new TokenPosition()), 12, new TokenPosition());
        assertEquals(arrayType.getSize(), testArrayType.getSize());
    }

    @Test
    public void buildCurrencyTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IDENTIFIER, "EUR"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        Currency currency = parser.tryBuildCurrency(
                new BigDecimal(new java.math.BigDecimal("12.432"), new TokenPosition()));
        assertEquals(currency.getValue(), new java.math.BigDecimal("12.432"));
        assertEquals(currency.getCurrencyAbbreviation().getAbbreviation(), "EUR");
    }

    @Test
    public void buildCurrencyConversionTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IDENTIFIER, "USD"));
        tokens.add(new Token(TokenType.OPEN_ROUND_BRACKET, "("));
        tokens.add(new Token(TokenType.BIG_DECIMAL_NUMBER, "12.25"));
        tokens.add(new Token(TokenType.IDENTIFIER, "EUR"));
        tokens.add(new Token(TokenType.CLOSE_ROUND_BRACKET, ")"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        CurrencyConversion currencyConversion = parser.tryBuildCurrencyConversion();

        assertEquals(currencyConversion.getCurrencyAbbreviation().getAbbreviation(), "USD");
        assertEquals(((BigDecimal)currencyConversion.getContent()).getValue(), new java.math.BigDecimal(12.25));
        assertEquals(currencyConversion.getCurrency().get().getAbbreviation(), "EUR");
    }

    @Test
    public void buildCurrencyAssignmentTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.BIG_DECIMAL_NUMBER, "612.5"));
        tokens.add(new Token(TokenType.IDENTIFIER, "RUB"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        CurrencyAssignment currencyAssignment = parser.tryBuildCurrencyAssignment();

        assertEquals(((BigDecimal) currencyAssignment.getContent()).getValue(), new java.math.BigDecimal(612.5));
        assertEquals(currencyAssignment.getCurrencyAbbreviation().getAbbreviation(), "RUB");
    }
}
