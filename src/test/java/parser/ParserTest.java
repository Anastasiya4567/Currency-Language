package parser;

import org.junit.jupiter.api.Test;
import parser.basic.*;
import parser.basic.Number;
import parser.expression.Expression;
import parser.type.ArrayType;
import parser.type.SimpleType;
import parser.type.Type;
import scanner.IScanner;
import scanner.Token;
import scanner.TokenPosition;
import scanner.TokenType;

import java.math.BigInteger;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
    class ScannerMock implements IScanner {
        private ArrayList<Token> tokens;
        private int index;
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
    public void buildConstBooleanTest() {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IDENTIFIER, "false"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        ConstBoolean constBoolean = parser.tryBuildConstBoolean();
        ConstBoolean testConstBoolean = new ConstBoolean("false", new TokenPosition());
        assertEquals(constBoolean.getValue(), testConstBoolean.getValue());
    }

    @Test
    public void buildNumberTest() {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.NUMBER, "312"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        Number number = parser.tryBuildNumber();
        Number testNumber = new Number(312, new TokenPosition());
        assertEquals(number.getValue(), testNumber.getValue());
    }

    @Test
    public void buildImportTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IDENTIFIER, "import"));
        tokens.add(new Token(TokenType.CONST_STRING, "currencies.json"));
        tokens.add(new Token(TokenType.SEMI_COLON, ";"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        Import _import = parser.tryBuildImport();
        Import testImport = new Import("currencies.json", new TokenPosition());
        assertEquals(_import.getFileName(), testImport.getFileName());
    }

    @Test
    public void buildBigDecimalTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.BIG_DECIMAL, "12.34"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        BigDecimal bigDecimal = parser.tryBuildBigDecimal();
        BigDecimal testBigDecimal = new BigDecimal(
                new java.math.BigDecimal("12.34"), new TokenPosition());
        assertTrue(bigDecimal.getValue().compareTo(testBigDecimal.getValue()) == 0);
    }

    @Test
    public void buildCurrencyAbbreviationTest() {
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
    public void buildCurrencyTest() {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IDENTIFIER, "EUR"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        Currency currency = parser.tryBuildCurrency(
                new BigDecimal(new java.math.BigDecimal("12.432"), new TokenPosition()));
        assertEquals(currency.getValue(), new java.math.BigDecimal("12.432"));
        assertEquals(currency.getCurrencyAbbreviation().getAbbreviation(), "EUR");
    }
}
