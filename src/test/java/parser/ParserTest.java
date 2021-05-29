package parser;

import org.junit.jupiter.api.Test;
import parser.basic.*;
import parser.body.FunctionCall;
import parser.body.VariableDeclaration;
import parser.body.VariableDefinition;
import parser.currency.CurrencyAbbreviation;
import parser.currency.CurrencyAssignment;
import parser.currency.CurrencyConversion;
import parser.expression.ComplexExpression;
import parser.expression.Expression;
import parser.expression.IfExpression;
import parser.expression.SimpleExpression;
import parser.type.ArrayType;
import parser.type.SimpleType;
import scanner.IScanner;
import scanner.token.Token;
import scanner.token.TokenPosition;
import scanner.token.TokenType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
    static class ScannerMock implements IScanner {
        private final ArrayList<Token> tokens;
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
    public void buildArgumentValueTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.BOOLEAN_VALUE, "true"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        ArgumentValue argumentValue = parser.tryBuildArgumentValue();
        assertEquals(argumentValue.getValue(), "true");
    }

    @Test
    public void buildVariableValueTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IDENTIFIER, "a"));
        tokens.add(new Token(TokenType.OPEN_SQUARE_BRACKET, "["));
        tokens.add(new Token(TokenType.IDENTIFIER, "i"));
        tokens.add(new Token(TokenType.CLOSE_SQUARE_BRACKET, "]"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        VariableValue variableValue = parser.tryBuildVariableValue();
        ArrayElementReference arrayElementReference = (ArrayElementReference) variableValue.getValue();
        Identifier arrayName = arrayElementReference.getArrayName();
        assertEquals(arrayName.getValue(), "a");
        Identifier index = (Identifier) arrayElementReference.getIndex();
        assertEquals(index.getValue(), "i");
    }

    @Test
    public void buildVariableDeclarationTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.INT, "int"));
        tokens.add(new Token(TokenType.IDENTIFIER, "i"));
        tokens.add(new Token(TokenType.ASSIGN, "="));
        tokens.add(new Token(TokenType.NUMBER, "4"));
        tokens.add(new Token(TokenType.SEMI_COLON, ";"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        VariableDeclaration variableDeclaration = parser.tryBuildVariableDeclaration();
        SimpleType simpleType = (SimpleType)variableDeclaration.getType();

        assertEquals(simpleType.getType(), "int");
        Expression expression = (Expression) variableDeclaration.getValue();
        Integer number = (Integer) expression.getValue();
        assertEquals(variableDeclaration.getIdentifier().getValue(), "i");
        assertEquals(number, 4);
    }

    @Test
    public void buildArgumentDeclarationTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.STRING, "String"));
        tokens.add(new Token(TokenType.IDENTIFIER, "str"));
        tokens.add(new Token(TokenType.ASSIGN, "="));
        tokens.add(new Token(TokenType.CONST_STRING, "Hello"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        ArgumentDeclaration argumentDeclaration = parser.tryBuildArgumentDeclaration();
        SimpleType stringType = (SimpleType)argumentDeclaration.getType();
        assertEquals(stringType.getType(), "String");

        Identifier name = argumentDeclaration.getIdentifier();
        assertEquals(name.getValue(), "str");

        ArgumentValue argumentValue = argumentDeclaration.getValue();
        assertEquals(argumentValue.getValue(), "Hello");
    }

    @Test
    public void buildVariableValueArrayTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.OPEN_CURLY_BRACKET, "{"));
        tokens.add(new Token(TokenType.NUMBER, "4"));
        tokens.add(new Token(TokenType.COMMA, ","));
        tokens.add(new Token(TokenType.NUMBER, "6"));
        tokens.add(new Token(TokenType.CLOSE_CURLY_BRACKET, "}"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        VariableValueArray variableValueArray = parser.tryBuildVariableValueArray();
        VariableValue variableValue = (VariableValue)variableValueArray.getValues().get(0);
        ArgumentValue argValue = (ArgumentValue) variableValue.getValue();
        Integer firstElement = (Integer)argValue.getValue();
        assertEquals(firstElement, 4);

        variableValue = (VariableValue)variableValueArray.getValues().get(1);
        argValue = (ArgumentValue) variableValue.getValue();
        Integer secondElement = (Integer)argValue.getValue();
        assertEquals(secondElement, 6);
    }

    @Test
    public void buildVariableDefinitionTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IDENTIFIER, "i"));
        tokens.add(new Token(TokenType.ASSIGN, "="));
        tokens.add(new Token(TokenType.NUMBER, "15"));
        tokens.add(new Token(TokenType.SEMI_COLON, ";"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        VariableDefinition variableDefinition = parser.tryBuildVariableDefinition(new Identifier("i", new TokenPosition()));

        Expression expression = (Expression) variableDefinition.getValue();
        Integer value = (Integer) expression.getValue();
        assertEquals(value, 15);
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

        Currency currency = parser.tryBuildCurrency(new java.math.BigDecimal("12.432"));
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
        assertEquals(currencyConversion.getContent(), new BigDecimal(12.25));
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

        assertEquals(currencyAssignment.getContent(), new BigDecimal(612.5));
        assertEquals(currencyAssignment.getCurrencyAbbreviation().getAbbreviation(), "RUB");
    }

    @Test
    public void buildSimpleExpressionTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.EXCLAMATION_MARK, "!"));
        tokens.add(new Token(TokenType.IDENTIFIER, "isConverted"));
        tokens.add(new Token(TokenType.OPEN_ROUND_BRACKET, "("));
        tokens.add(new Token(TokenType.IDENTIFIER, "a"));
        tokens.add(new Token(TokenType.CLOSE_ROUND_BRACKET, ")"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        SimpleExpression simpleExpression = parser.tryBuildSimpleExpression();

        assertTrue(simpleExpression.isNegated());

        FunctionCall functionCall = (FunctionCall) simpleExpression.getContent();
        String name = functionCall.getName().getValue();
        assertEquals(name, "isConverted");

        Expression argument = (Expression) functionCall.getArguments().get(0);
        String argumentName = ((Identifier) argument.getValue()).getValue();
        assertEquals(argumentName, "a");
    }

    @Test
    public void buildComplexExpressionTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.EXCLAMATION_MARK, "!"));
        tokens.add(new Token(TokenType.IDENTIFIER, "a"));
        tokens.add(new Token(TokenType.LESS_THAN, "<"));
        tokens.add(new Token(TokenType.IDENTIFIER, "b"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        Object simpleOrComplexExpression = parser.tryBuildComplexExpression();

        ComplexExpression complexExpression = (ComplexExpression) simpleOrComplexExpression;
        SimpleExpression leftSimpleExpression = complexExpression.getLeftSimpleExpression();
        assertTrue(leftSimpleExpression.isNegated());

        Expression expression = (Expression) leftSimpleExpression.getContent();
        String value = ((Identifier )expression.getValue()).getValue();

        assertEquals (value, "a");
        assertEquals(complexExpression.getRelationalOperator(), "<");
        assertFalse(complexExpression.getRightSimpleExpression().isNegated());

        SimpleExpression rightSimpleExpression = complexExpression.getRightSimpleExpression();
        Expression expression2 = (Expression) rightSimpleExpression.getContent();
        String value2 = ((Identifier )expression2.getValue()).getValue();
        assertEquals(value2, "b");
    }

    @Test
    public void buildIfExpressionTest() throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IDENTIFIER, "a"));
        tokens.add(new Token(TokenType.MORE_OR_EQUALS, ">="));
        tokens.add(new Token(TokenType.IDENTIFIER, "b"));
        tokens.add(new Token(TokenType.AND, "&&"));
        tokens.add(new Token(TokenType.IDENTIFIER, "c"));
        ScannerMock scanner = new ScannerMock(tokens);
        Parser parser = new Parser(scanner);

        IfExpression ifExpression = parser.tryBuildIfExpression();

        ComplexExpression complexExpression = (ComplexExpression) ifExpression.getExpression();
        SimpleExpression leftSimpleExpression = complexExpression.getLeftSimpleExpression();
        assertFalse(leftSimpleExpression.isNegated());

        Expression expression = (Expression) leftSimpleExpression.getContent();
        String value = ((Identifier )expression.getValue()).getValue();

        assertEquals (value, "a");
        assertEquals(complexExpression.getRelationalOperator(), ">=");
        assertFalse(complexExpression.getRightSimpleExpression().isNegated());

        SimpleExpression rightSimpleExpression = complexExpression.getRightSimpleExpression();
        Expression expression2 = (Expression) rightSimpleExpression.getContent();
        String value2 = ((Identifier )expression2.getValue()).getValue();
        assertEquals(value2, "b");

        Optional<IfExpression> innerIfExpression = ifExpression.getIfExpression();
        SimpleExpression simpleExpression2 = (SimpleExpression) innerIfExpression.get().getExpression();
        Expression expression3 = (Expression) simpleExpression2.getContent();
        String value3 = ((Identifier )expression3.getValue()).getValue();
        assertEquals(value3, "c");
    }

    @Test
    public void buildConditionTest() throws Exception {
    }
}
