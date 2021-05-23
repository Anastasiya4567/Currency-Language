package parser;

import parser.basic.*;
import parser.basic.Number;
import parser.body.*;
import parser.currency.CurrencyAbbreviation;
import parser.currency.CurrencyAssignment;
import parser.currency.CurrencyConversion;
import parser.exception.UnexpectedTokenException;
import parser.expression.*;
import parser.type.ArrayType;
import parser.type.SimpleType;
import parser.type.Type;
import scanner.token.Token;
import scanner.token.TokenPosition;
import scanner.token.TokenType;
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
        if (currentToken.getTokenType() == TokenType.IMPORT) {
            TokenPosition startTokenPosition = currentToken.getTokenPosition();
            if (getNextToken().getTokenType() != TokenType.CONST_STRING)
                throw new UnexpectedTokenException("string constant", currentToken);
            String fileName = currentToken.getValue();
            if (getNextToken().getTokenType() != TokenType.SEMI_COLON)
                throw new UnexpectedTokenException(";", currentToken);
            getNextToken();
            return new Import(fileName, startTokenPosition);
        }
        return null;
    }

    private SimpleType tryBuildSimpleType () {
        if (currentToken.getTokenType() == TokenType.CURRENCY ||
            currentToken.getTokenType() == TokenType.BOOLEAN ||
            currentToken.getTokenType() == TokenType.BIG_DECIMAL ||
            currentToken.getTokenType() == TokenType.INT ||
            currentToken.getTokenType() == TokenType.STRING)
            return new SimpleType(currentToken.getValue(), currentToken.getTokenPosition());
        return null;
    }

    public Type tryBuildType() throws Exception {
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
        }
        Integer size = Integer.parseInt(currentToken.getValue());
        if (getNextToken().getTokenType() != TokenType.CLOSE_SQUARE_BRACKET)
            throw new UnexpectedTokenException("]", currentToken);
        return new ArrayType(simpleType, size, currentToken.getTokenPosition());
    }

    private Identifier tryBuildIdentifier () throws Exception {
        TokenPosition startTokenPosition = currentToken.getTokenPosition();
        if (currentToken.getTokenType() != TokenType.IDENTIFIER)
            return null;
        getNextToken();
        return new Identifier(currentToken.getValue(), startTokenPosition);
    }

    public CurrencyAbbreviation tryBuildCurrencyAbbreviation () throws Exception {
        if (this.currentToken.getTokenType() == TokenType.IDENTIFIER && currentToken.getValue().length() == 3) {
            StringBuilder abbreviation = new StringBuilder();
            for (int i = 0; i < currentToken.getValue().length(); i++) {
                if (currentToken.getValue().charAt(i) < 'A' || currentToken.getValue().charAt(i) > 'Z')
                    return null;
                abbreviation.append(currentToken.getValue().charAt(i));
            }
            getNextToken();
            return new CurrencyAbbreviation(abbreviation.toString(), currentToken.getTokenPosition());
        }
        return null;
    }

    public BigDecimal tryBuildBigDecimal () throws Exception {
        if (this.currentToken.getTokenType() != TokenType.BIG_DECIMAL_NUMBER)
            return null;
        Token token = currentToken;
        getNextToken();
        return new BigDecimal(new java.math.BigDecimal(token.getValue()), token.getTokenPosition());
    }

    private CurrencyConversion tryBuildCurrencyConversionBody(CurrencyAbbreviation currencyAbbreviation, Object content) throws Exception {
        if (currentToken.getTokenType() == TokenType.CLOSE_CURLY_BRACKET) {
            getNextToken();
            return new CurrencyConversion(currencyAbbreviation, content, null, currentToken.getTokenPosition());
        } else {
            CurrencyAbbreviation optionalCurrencyAbbreviation = tryBuildCurrencyAbbreviation();
            if (optionalCurrencyAbbreviation == null)
                throw new UnexpectedTokenException("currency abbreviation or )", currentToken);
            if (currentToken.getTokenType() == TokenType.CLOSE_ROUND_BRACKET) {
                getNextToken();
                return new CurrencyConversion(currencyAbbreviation, content,
                        Optional.of(optionalCurrencyAbbreviation), currentToken.getTokenPosition());
            }
            throw new UnexpectedTokenException(")", currentToken);
        }
    }

    private CurrencyConversion tryBuildCurrencyContent(CurrencyAbbreviation currencyAbbreviation) throws Exception {
        Number number = tryBuildNumber();
        if (number != null)
            return tryBuildCurrencyConversionBody(currencyAbbreviation, number);

        BigDecimal bigDecimal = tryBuildBigDecimal();
        if (bigDecimal != null)
            return tryBuildCurrencyConversionBody(currencyAbbreviation, bigDecimal);

        Identifier identifier = tryBuildIdentifier();
        if (identifier != null) {
            ArrayElementReference arrayElementReference = tryBuildArrayElementReference(identifier);
            if (arrayElementReference == null)
                throw new UnexpectedTokenException("number or reference to array element", currentToken);
            else
                return tryBuildCurrencyConversionBody(currencyAbbreviation, arrayElementReference);
        }
        throw new UnexpectedTokenException("number or reference to array element", currentToken);
    }

    public CurrencyConversion tryBuildCurrencyConversion() throws Exception {
        CurrencyAbbreviation currencyAbbreviation = tryBuildCurrencyAbbreviation();
        if (currencyAbbreviation == null)
            return null;
        if (currentToken.getTokenType() != TokenType.OPEN_ROUND_BRACKET)
            // ??????
            return null;
        getNextToken();
        return tryBuildCurrencyContent(currencyAbbreviation);
    }

    private CurrencyAssignment tryBuildCurrencyAssignmentBody(Object content) throws Exception {
        CurrencyAbbreviation currencyAbbreviation = tryBuildCurrencyAbbreviation();
        if (currencyAbbreviation == null)
            throw new UnexpectedTokenException("currency abbreviation", currentToken);
        getNextToken();
        return new CurrencyAssignment(content, currencyAbbreviation, currentToken.getTokenPosition());
    }

    public CurrencyAssignment tryBuildCurrencyAssignment() throws Exception {
        Number number = tryBuildNumber();
        if (number != null)
            return tryBuildCurrencyAssignmentBody(number);

        BigDecimal bigDecimal = tryBuildBigDecimal();
        if (bigDecimal != null)
            return tryBuildCurrencyAssignmentBody(bigDecimal);

        Identifier identifier = tryBuildIdentifier();
        if (identifier != null) {
            ArrayElementReference arrayElementReference = tryBuildArrayElementReference(identifier);
            if (arrayElementReference == null)
                throw new UnexpectedTokenException("number or reference to array element", currentToken);
            else
                return tryBuildCurrencyAssignmentBody(arrayElementReference);
        }
        throw new UnexpectedTokenException("number or reference to array element", currentToken);
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

    public Currency tryBuildCurrency (BigDecimal bigDecimal) throws Exception {
        CurrencyAbbreviation currencyAbbreviation = tryBuildCurrencyAbbreviation();
        if (currencyAbbreviation == null)
            return null;
        getNextToken();
        return new Currency(bigDecimal.getValue(), currencyAbbreviation, currentToken.getTokenPosition());
    }

    public Number tryBuildNumber () throws Exception {
        if (currentToken.getTokenType() != TokenType.NUMBER)
            return null;
        TokenPosition startTokenPosition = currentToken.getTokenPosition();
        getNextToken();
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
            getNextToken();
            return new ArrayElementReference(identifier, number, currentToken.getTokenPosition());
        }
        if (getNextToken().getTokenType() == TokenType.CLOSE_SQUARE_BRACKET)
            throw new UnexpectedTokenException("]", currentToken);
        getNextToken();
        return new ArrayElementReference(identifier, indexIdentifier, currentToken.getTokenPosition());
    }

    public Object tryBuildSimpleExpressionContent () throws Exception {
        Expression expression = tryBuildExpression();
        if (expression != null)
            return expression;
        getNextToken();
        Identifier identifier = tryBuildIdentifier();
        if (identifier == null)
            return null;
        FunctionCall functionCall = tryBuildFunctionCall(identifier);
        return functionCall;
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
            getNextToken();
            return new Condition(isNegated, ifExpression, Optional.ofNullable(condition), tokenPosition);
        }
        getNextToken();
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

    public VariableDefinition tryBuildVariableDefinition (Identifier identifier) throws Exception {
        ArrayElementReference arrayElementReference = tryBuildArrayElementReference(identifier);
        return tryBuildVariableDefinitionValue(Objects.requireNonNullElse(arrayElementReference, identifier));
    }

    public VariableValue tryBuildVariableValue() throws Exception {
        Identifier identifier = tryBuildIdentifier();
        if (identifier == null)
            return null;
        ArrayElementReference arrayElementReference = tryBuildArrayElementReference(identifier);
        if (arrayElementReference != null)
            return new VariableValue(arrayElementReference, currentToken.getTokenPosition());

        // if arg value
        //
        //

        FunctionCall functionCall = tryBuildFunctionCall(identifier);
        if (functionCall != null)
            return new VariableValue(functionCall, currentToken.getTokenPosition());
        throw new UnexpectedTokenException("[, ) or ...", currentToken);
    }

    private ArrayList<Object> tryBuildFunctionCallArguments () throws Exception {
        getNextToken();
        ArrayList<Object> arguments = new ArrayList<>();
        while (currentToken.getTokenType() != TokenType.CLOSE_ROUND_BRACKET) {
            getNextToken();
            VariableValueArray variableValueArray = tryBuildVariableValueArray();
            if (variableValueArray != null)
                arguments.add(variableValueArray);
            else {
                Expression expression = tryBuildExpression();
                if (expression == null)
                    throw new UnexpectedTokenException("expression", currentToken);
                arguments.add(expression);
            }
            if (getNextToken().getTokenType() == TokenType.COMMA
                    || currentToken.getTokenType() == TokenType.CLOSE_ROUND_BRACKET)
                continue;
            throw new UnexpectedTokenException(", or )", currentToken);
        }
        return arguments;
    }

    public VariableValueArray tryBuildVariableValueArray() throws Exception {
        if (currentToken.getTokenType() != TokenType.OPEN_CURLY_BRACKET)
            return null;
        ArrayList<Object> values = new ArrayList<>();
        while (currentToken.getTokenType() != TokenType.CLOSE_CURLY_BRACKET) {
            getNextToken();
            Object value = tryBuildVariableValue();
            if (value == null)
                throw new UnexpectedTokenException("identifier", currentToken);
            values.add(value);

//                VariableDefinition variableDefinition = tryBuildVariableDefinition(identifier);
//                if (variableDefinition != null)
//                    values.add(variableDefinition);
//                else throw new UnexpectedTokenException("identifier or function call", currentToken);
            if (getNextToken().getTokenType() == TokenType.COMMA
                    || currentToken.getTokenType() == TokenType.CLOSE_CURLY_BRACKET)
                continue;
            throw new UnexpectedTokenException(", or )", currentToken);
        }
        return new VariableValueArray(values, currentToken.getTokenPosition());
    }

    public FunctionCall tryBuildFunctionCall (Identifier identifier) throws Exception {
        if (currentToken.getTokenType() == TokenType.OPEN_ROUND_BRACKET)
            return new FunctionCall(identifier, tryBuildFunctionCallArguments(), currentToken.getTokenPosition());
        return null;
    }

    public void tryBuildIfPartOfIfStatement() {

    }

    public IfStatement tryBuildIfStatement() throws Exception {
        if (currentToken.getValue() != "if")
            return null;
        if (getNextToken().getTokenType() != TokenType.OPEN_ROUND_BRACKET)
            throw new UnexpectedTokenException("(", currentToken);
        getNextToken();
        Condition condition = tryBuildCondition();
        if (condition == null)
            throw new UnexpectedTokenException("condition", currentToken);
        if (currentToken.getTokenType() != TokenType.CLOSE_ROUND_BRACKET)
            throw new UnexpectedTokenException(")", currentToken);
        if (getNextToken().getTokenType() != TokenType.OPEN_CURLY_BRACKET)
            throw new UnexpectedTokenException("{", currentToken);
        getNextToken();
        Body ifBody = tryBuildBody();
        if (currentToken.getTokenType() != TokenType.CLOSE_CURLY_BRACKET)
            throw new UnexpectedTokenException("}", currentToken);
        if (getNextToken().getValue() != "else")
            return new IfStatement(condition, null, null, null);
        getNextToken();
        IfStatement ifStatement = tryBuildIfStatement();
        if (ifStatement != null) {
            getNextToken();
            return new IfStatement(condition, null, null, Optional.of(ifStatement));
        }

        if (currentToken.getTokenType() != TokenType.OPEN_CURLY_BRACKET)
            throw new UnexpectedTokenException("{", currentToken);
        getNextToken();
        Body elseBody = tryBuildBody();
        if (currentToken.getTokenType() != TokenType.CLOSE_CURLY_BRACKET)
            throw new UnexpectedTokenException("}", currentToken);
        return new IfStatement(condition, Optional.of(ifBody), Optional.of(elseBody), Optional.of(ifStatement));
    }

    //TO DO
    public Body tryBuildBody() throws Exception {
        IfStatement ifStatement = tryBuildIfStatement();
        if (ifStatement != null)
            return null;
        ForStatement forStatement = tryBuildForStatement();
        if (forStatement != null)
            return null;
        VariableDeclaration variableDeclaration = tryBuildVariableDeclaration();
        if (variableDeclaration != null)
            return null;
        if (currentToken.getTokenType() == TokenType.RETURN)
            return null;
        Identifier identifier = tryBuildIdentifier();
        if (identifier == null)
            return null;
        FunctionCall functionCall = tryBuildFunctionCall(identifier);
        if (functionCall != null)
            return null;
        VariableDefinition variableDefinition = tryBuildVariableDefinition(identifier);
        if (variableDefinition != null)
            return null;
        return null;
    }

    public ForExpression tryBuildForExpression() throws Exception {
        VariableDeclaration variableDeclaration = tryBuildVariableDeclaration();
        if (variableDeclaration == null)
            return null;
        Object complexExpression = tryBuildComplexExpression();
        if (complexExpression == null)
            throw new UnexpectedTokenException("expression", currentToken);
        if (currentToken.getTokenType() != TokenType.SEMI_COLON)
            throw new UnexpectedTokenException(";", currentToken);
        Expression expression = tryBuildExpression();
        if (expression == null)
            throw new UnexpectedTokenException("expression", currentToken);
        return new ForExpression(variableDeclaration,complexExpression, expression);
    }

    public ForStatement tryBuildForStatement() throws Exception {
        if (currentToken.getTokenType() != TokenType.FOR)
            return null;
        if (getNextToken().getTokenType() != TokenType.OPEN_ROUND_BRACKET)
            throw new UnexpectedTokenException("(", currentToken);
        getNextToken();
        ForExpression forExpression = tryBuildForExpression();
        if (forExpression == null)
            throw new UnexpectedTokenException("expression", currentToken);
        if (currentToken.getTokenType() != TokenType.CLOSE_ROUND_BRACKET)
            throw new UnexpectedTokenException(")", currentToken);
        if (getNextToken().getTokenType() != TokenType.OPEN_CURLY_BRACKET)
            throw new UnexpectedTokenException("{", currentToken);
        Body body = tryBuildBody();
        // ?????????????
        if (body == null)
            throw new UnexpectedTokenException("expression", currentToken);
        if (currentToken.getTokenType() != TokenType.CLOSE_CURLY_BRACKET)
            throw new UnexpectedTokenException("{", currentToken);
        return new ForStatement(forExpression, body);
    }

    private boolean isReturnType() throws Exception {
        if (currentToken.getTokenType() == TokenType.VOID)
            return true;
        Type type = tryBuildType();
        if (type == null)
            return false;
        return true;
    }

    private ArrayList<ArgumentDeclaration> tryBuildFunctionDeclarationArguments() throws Exception {
        ArrayList<ArgumentDeclaration> argumentDeclarations = new ArrayList<>();
        while (currentToken.getTokenType() != TokenType.CLOSE_ROUND_BRACKET) {
            if (currentToken.getTokenType() == TokenType.COMMA && argumentDeclarations.size() > 0)
                getNextToken();
            ArgumentDeclaration argumentDeclaration = tryBuildArgumentDeclaration();
            if (argumentDeclaration == null)
                throw new UnexpectedTokenException("argument declaration", currentToken);
            argumentDeclarations.add(argumentDeclaration);
            if (currentToken.getTokenType() != TokenType.COMMA && currentToken.getTokenType() != TokenType.CLOSE_ROUND_BRACKET)
                throw new UnexpectedTokenException(", or )", currentToken);
        }
        return argumentDeclarations;
    }

    private Body tryBuildFunctionDeclarationBody() throws Exception {
        if (getNextToken().getTokenType() != TokenType.OPEN_CURLY_BRACKET)
            throw new UnexpectedTokenException("{", currentToken);
        if (getNextToken().getTokenType() == TokenType.CLOSE_CURLY_BRACKET)
            return null;
        Body body = tryBuildBody();
        if (body == null)
            throw new UnexpectedTokenException("expressions or }", currentToken);
        if (getNextToken().getTokenType() == TokenType.CLOSE_CURLY_BRACKET)
            return body;
        throw new UnexpectedTokenException("}", currentToken);
    }

    // FuncDeclaration = ReturnType, Identifier, ‘(‘, [ArgDeclaration, {‘,’, ArgDeclaration },], ‘)’, ‘{‘, { Body },  ‘}’;
    public FunctionDeclaration tryBuildFunctionDeclaration() throws Exception {
        Token startToken = currentToken;
        if (!isReturnType())
            return null;
        Identifier identifier = tryBuildIdentifier();
        if (identifier == null)
            throw new UnexpectedTokenException("identifier", currentToken);
        if (currentToken.getTokenType() != TokenType.OPEN_ROUND_BRACKET)
            throw new UnexpectedTokenException("(", currentToken);
        if (getNextToken().getTokenType() == TokenType.CLOSE_ROUND_BRACKET) {
            Body body = tryBuildFunctionDeclarationBody();
            return new FunctionDeclaration(startToken.getValue(), null, body, currentToken.getTokenPosition());
        }
        ArrayList<ArgumentDeclaration> argumentDeclarations = tryBuildFunctionDeclarationArguments();
        Body body = tryBuildFunctionDeclarationBody();
        return new FunctionDeclaration(startToken.getValue(), argumentDeclarations, body, currentToken.getTokenPosition());
    }

    public ArgumentDeclaration tryBuildArgumentDeclaration() {
        return null;
    }

    private void parseToken () {
    }
}
