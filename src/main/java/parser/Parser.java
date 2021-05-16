package parser;

import parser.basic.*;
import parser.basic.Number;
import parser.expression.ComplexExpression;
import parser.expression.Expression;
import parser.expression.IfExpression;
import parser.expression.SimpleExpression;
import parser.type.ArrayType;
import parser.type.SimpleType;
import parser.type.Type;
import scanner.Token;
import scanner.TokenPosition;
import scanner.TokenType;
import scanner.IScanner;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class Parser {
    private final IScanner scanner;
    private Token currentToken;
    private ArrayList<String> knownCurrencies;


    public Parser(IScanner scanner) {
        this.scanner = scanner;
        this.currentToken = scanner.getCurrentToken();
//        this.knownCurrencies = new ArrayList<>("USD", "EUR", "PLN");
    }

    public void parse() {
        Token currentToken = scanner.getCurrentToken();
        parseToken();
    }

    private Token getNextToken() throws Exception {
        scanner.next();
        this.currentToken = scanner.getCurrentToken();
        return this.currentToken;
    }

    public Import tryBuildImport() throws Exception {
        if (currentToken.getTokenType() == TokenType.IDENTIFIER && currentToken.getValue().equals("import")) {
            if (getNextToken().getTokenType() != TokenType.CONST_STRING)
                throw new UnexpectedTokenException("string constant", currentToken);
            String fileName = currentToken.getValue();
            if (getNextToken().getTokenType() != TokenType.SEMI_COLON)
                throw new UnexpectedTokenException(";", currentToken);
            return new Import(fileName, currentToken.getTokenPosition());
        }
        return null;
    }

    public ConstBoolean tryBuildConstBoolean () {
        if (currentToken.getTokenType() == TokenType.IDENTIFIER &&
                (currentToken.getValue().equals("true") || currentToken.getValue().equals("false")))
            return new ConstBoolean(currentToken.getValue(), currentToken.getTokenPosition());
        return null;
    }

    private SimpleType tryBuildSimpleType () {
        if (currentToken.getTokenType() == TokenType.IDENTIFIER &&
                (currentToken.getValue().equals("int") || currentToken.getValue().equals("boolean")
                        || currentToken.getValue().equals("BigDecimal") || currentToken.getValue().equals("Currency")
                        || currentToken.getValue().equals("String"))) {
            return new SimpleType(currentToken.getValue(), currentToken.getTokenPosition());
        }
        return null;
    }

    public Type tryBuildType () throws Exception {
        SimpleType simpleType = tryBuildSimpleType();
        if (simpleType == null)
            return null;
        Token startingToken = scanner.getCurrentToken();
        getNextToken();
        ArrayType arrayType = tryBuildArrayType(simpleType);
        if (arrayType != null)
            return arrayType;
        return simpleType;
    }

    public ArrayType tryBuildArrayType (SimpleType simpleType) throws Exception {
        if (currentToken.getTokenType() != TokenType.OPEN_SQUARE_BRACKET)
            return null;
        if (getNextToken().getTokenType() != TokenType.NUMBER) {
            if (currentToken.getTokenType() != TokenType.CLOSE_SQUARE_BRACKET)
                throw new UnexpectedTokenException("]", currentToken);
            return new ArrayType(simpleType, null, currentToken.getTokenPosition());
        } else {
            Integer size = Integer.parseInt(currentToken.getValue());
            if (getNextToken().getTokenType() != TokenType.CLOSE_SQUARE_BRACKET)
                throw new UnexpectedTokenException("]", currentToken);
            return new ArrayType(simpleType, size, currentToken.getTokenPosition());
        }
    }

    private Identifier tryBuildIdentifier () {
        if (currentToken.getTokenType() == TokenType.IDENTIFIER)
            return new Identifier(currentToken.getValue(), currentToken.getTokenPosition());
        return null;
    }

    public CurrencyAbbreviation tryBuildCurrencyAbbreviation () {
        if (this.currentToken.getTokenType() == TokenType.IDENTIFIER && currentToken.getValue().length() == 3) {
            StringBuilder abbreviation = new StringBuilder();
            for (int i = 0; i < currentToken.getValue().length(); i++) {
                if (currentToken.getValue().charAt(i) < 'A' || currentToken.getValue().charAt(i) > 'Z')
                    return null;
                abbreviation.append(currentToken.getValue().charAt(i));
            }
            return new CurrencyAbbreviation(abbreviation.toString(), currentToken.getTokenPosition());
        }
        return null;
    }

    public BigDecimal tryBuildBigDecimal () throws Exception {
        if (this.currentToken.getTokenType() != TokenType.BIG_DECIMAL)
            return null;
        Token token = currentToken;
        getNextToken();
        return new BigDecimal(new java.math.BigDecimal(token.getValue()), token.getTokenPosition());
    }

    public Expression tryBuildExpression () throws Exception {
        Identifier identifier = tryBuildIdentifier();
        if (identifier != null)
            //TRY BUILD FUNCTION CALL OR CURRENCY CONVERSION
            return new Expression(identifier, currentToken.getTokenPosition());
        Number number = tryBuildNumber();
        if (number != null)
            return new Expression(number, currentToken.getTokenPosition());
        BigDecimal bigDecimal = tryBuildBigDecimal();
        if (bigDecimal == null)
            return null;
        Currency currency = tryBuildCurrency(bigDecimal);
        if (currency != null)
            return new Expression(currency, currentToken.getTokenPosition());
        return new Expression(bigDecimal, currentToken.getTokenPosition());
    }

    public Currency tryBuildCurrency (BigDecimal bigDecimal){
        CurrencyAbbreviation currencyAbbreviation = tryBuildCurrencyAbbreviation();
        if (currencyAbbreviation == null)
            return null;
        return new Currency(bigDecimal.getValue(), currencyAbbreviation, currentToken.getTokenPosition());
    }

    public Number tryBuildNumber () {
        if (currentToken.getTokenType() != TokenType.NUMBER)
            return null;
        return new Number(Integer.parseInt(currentToken.getValue()), currentToken.getTokenPosition());
    }

    public ArrayElementReference tryBuildArrayElementReference (Identifier identifier) throws Exception {
        if (currentToken.getTokenType() != TokenType.OPEN_SQUARE_BRACKET)
            return null;
        Token nextToken = getNextToken();
        Identifier indexIdentifier = tryBuildIdentifier();
        if (indexIdentifier == null) {
            Number number = tryBuildNumber();
            if (number == null)
                return null;
            if (getNextToken().getTokenType() != TokenType.CLOSE_SQUARE_BRACKET)
                throw new UnexpectedTokenException("]", currentToken);
            return new ArrayElementReference(identifier, number, currentToken.getTokenPosition());
        }
        if (getNextToken().getTokenType() == TokenType.CLOSE_SQUARE_BRACKET)
            throw new UnexpectedTokenException("]", currentToken);
        return new ArrayElementReference(identifier, indexIdentifier, currentToken.getTokenPosition());
    }

    public Object tryBuildSimpleExpressionContent () throws Exception {
        Expression expression = tryBuildExpression();
        if (expression != null)
            return expression;
        FunctionCall functionCall = tryBuildFunctionCall();
        if (functionCall != null)
            return functionCall;
        return null;
    }

    public SimpleExpression tryBuildSimpleExpression () throws Exception {
        if (currentToken.getTokenType() == TokenType.EXCLAMATION_MARK) {
            getNextToken();
            if (tryBuildSimpleExpressionContent() == null)
                throw new UnexpectedTokenException("expression", currentToken);
            return new SimpleExpression(true, tryBuildSimpleExpressionContent(), currentToken.getTokenPosition());
        } else if (tryBuildSimpleExpressionContent() == null)
            return null;
        return new SimpleExpression(false, tryBuildSimpleExpressionContent(), currentToken.getTokenPosition());
    }

    public boolean isRelationalOperator () {
        return (currentToken.getTokenType() == TokenType.LESS_THAN
                || currentToken.getTokenType() == TokenType.LESS_OR_EQUALS
                || currentToken.getTokenType() == TokenType.MORE_THAN
                || currentToken.getTokenType() == TokenType.MORE_OR_EQUALS
                || currentToken.getTokenType() == TokenType.NOT_EQUALS);
    }

    public Object tryBuildComplexExpression () throws Exception {
        SimpleExpression leftSimpleExpression = tryBuildSimpleExpression();
        if (leftSimpleExpression == null)
            return null;

        getNextToken();
        if (!isRelationalOperator())
           return leftSimpleExpression;
        String relationalOperator = currentToken.getValue();

        getNextToken();
        SimpleExpression rightSimpleExpression = tryBuildSimpleExpression();
        if (rightSimpleExpression == null)
            throw new UnexpectedTokenException("expression", currentToken);
        return new ComplexExpression(leftSimpleExpression, relationalOperator, rightSimpleExpression, currentToken.getTokenPosition());
    }

    public IfExpression tryBuildIfExpression () throws Exception {
        TokenPosition tokenPosition = currentToken.getTokenPosition();
        Object simpleOrComplexExpression = tryBuildComplexExpression();
        if (simpleOrComplexExpression == null)
            return null;
        if (getNextToken().getTokenType() == TokenType.AND) {
            getNextToken();
            IfExpression ifExpression = tryBuildIfExpression();
            if (ifExpression == null)
                throw new UnexpectedTokenException("expression", currentToken);
            return new IfExpression(ifExpression, Optional.ofNullable(ifExpression), tokenPosition);
        }
        return new IfExpression(simpleOrComplexExpression, Optional.ofNullable(null), tokenPosition);
    }

    public Condition tryBuildCondition () throws Exception {
        TokenPosition tokenPosition = currentToken.getTokenPosition();
        boolean isNegated = false;
        if (currentToken.getTokenType() == TokenType.EXCLAMATION_MARK) {
            isNegated = true;
            getNextToken();
        }
        IfExpression ifExpression = tryBuildIfExpression();
        if (ifExpression == null)
            throw new UnexpectedTokenException("condition", currentToken);
        if (currentToken.getTokenType() == TokenType.OR) {
            getNextToken();
            Condition condition = tryBuildCondition();
            if (condition == null)
                throw new UnexpectedTokenException("condition", currentToken);
            return new Condition(isNegated, ifExpression, Optional.ofNullable(condition), tokenPosition);
        }
        return new Condition(isNegated, ifExpression, Optional.ofNullable(null), tokenPosition);
    }

    public VariableDeclaration tryBuildVariableDeclaration () throws Exception {
        Type type = tryBuildType();
        if (type == null)
            return null;
        Identifier identifier = tryBuildIdentifier();
        if (identifier == null)
            return null;
        if (getNextToken().getTokenType() == TokenType.ASSIGN) {
            getNextToken();
            Expression expression = tryBuildExpression();
            if (expression == null)
                throw new UnexpectedTokenException("expression", currentToken);
            if (getNextToken().getTokenType() == TokenType.SEMI_COLON)
                return new VariableDeclaration(type, identifier, expression, currentToken.getTokenPosition());
        } else {
            if (currentToken.getTokenType() == TokenType.SEMI_COLON)
                return new VariableDeclaration(type, identifier, null, currentToken.getTokenPosition());
        }
        throw new UnexpectedTokenException(";", currentToken);
    }

    public VariableDefinition tryBuildVariableDefinitionValue (Object name) throws Exception {
        if (getNextToken().getTokenType() != TokenType.ASSIGN)
            throw new UnexpectedTokenException("=", currentToken);
        Expression expression = tryBuildExpression();
        if (expression == null)
            throw new UnexpectedTokenException("expression", currentToken);
        if (getNextToken().getTokenType() == TokenType.SEMI_COLON) {
            return new VariableDefinition(name, expression, currentToken.getTokenPosition());
        }
        throw new UnexpectedTokenException(";", currentToken);
    }

    public VariableDefinition tryBuildVariableDefinition () throws Exception {
        Identifier identifier = tryBuildIdentifier();
        if (identifier == null)
            return null;
        ArrayElementReference arrayElementReference = tryBuildArrayElementReference(identifier);
        return tryBuildVariableDefinitionValue(Objects.requireNonNullElse(arrayElementReference, identifier));
    }

    public ArrayList<Object> tryBuildFunctionCallArguments () throws Exception {
        ArrayList<Object> arguments = new ArrayList<>();
        while (currentToken.getTokenType() != TokenType.CLOSE_ROUND_BRACKET) {
            getNextToken();
            Expression expression = tryBuildExpression();
            if (expression == null)
                throw new UnexpectedTokenException("expression", currentToken);
            arguments.add(expression);
            if (getNextToken().getTokenType() == TokenType.COMMA
                    || currentToken.getTokenType() == TokenType.CLOSE_ROUND_BRACKET)
                continue;
            throw new UnexpectedTokenException(", or )", currentToken);
        }
        return arguments;
    }

    public FunctionCall tryBuildFunctionCall () throws Exception {
        Identifier identifier = tryBuildIdentifier();
        if (identifier == null)
            return null;
        if (getNextToken().getTokenType() == TokenType.OPEN_ROUND_BRACKET)
            return new FunctionCall(identifier, tryBuildFunctionCallArguments(), currentToken.getTokenPosition());
        return null;
    }

    private void parseToken () {
    }
}
