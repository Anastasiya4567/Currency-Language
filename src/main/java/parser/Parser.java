package parser;

import parser.basic.*;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Integer.parseInt;

public class Parser {
    private final IScanner scanner;
    private Token currentToken;
    private ArrayList<String> knownCurrencies;

    public Parser(IScanner scanner) {
        this.scanner = scanner;
        this.currentToken = scanner.getCurrentToken();
//        this.knownCurrencies = new ArrayList<>("USD", "EUR", "PLN");
    }

    public void parse() throws Exception {
        Token currentToken = scanner.getCurrentToken();
        Program program = tryBuildProgram();
    }

    private Token getNextToken() throws Exception {
        scanner.next();
        this.currentToken = scanner.getCurrentToken();
        return this.currentToken;
    }

    public Program tryBuildProgram() throws Exception {
        Import _import = tryBuildImport();
        if (_import == null)
            return null;
        FunctionDeclaration functionDeclaration = tryBuildFunctionDeclaration();
        if (functionDeclaration == null)
            return null;

        MainFunction mainFunction = tryBuildMainFunction();
        if (mainFunction == null)
            throw new UnexpectedTokenException("main function declaration", currentToken);
        return new Program(null, mainFunction, currentToken.getTokenPosition());
    }

    public MainFunction tryBuildMainFunction() throws Exception {
        if (currentToken.getTokenType() != TokenType.INT)
            return null;
        if (getNextToken().getTokenType() != TokenType.MAIN_FUNCTION)
            throw new UnexpectedTokenException("main", currentToken);
        if (getNextToken().getTokenType() != TokenType.OPEN_ROUND_BRACKET)
            throw new UnexpectedTokenException("(", currentToken);
        if (getNextToken().getTokenType() != TokenType.CLOSE_ROUND_BRACKET)
            throw new UnexpectedTokenException(")", currentToken);
        if (getNextToken().getTokenType() != TokenType.OPEN_CURLY_BRACKET)
            throw new UnexpectedTokenException("{", currentToken);
        getNextToken();
        Instruction instruction;
        ArrayList<Instruction> instructions = new ArrayList<>();
        while((instruction = tryBuildInstruction()) != null)
            instructions.add(instruction);
        if (currentToken.getTokenType() != TokenType.CLOSE_CURLY_BRACKET)
            throw new UnexpectedTokenException("}", currentToken);
        return new MainFunction(instructions, currentToken.getTokenPosition());
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
        Token  startingToken = scanner.getCurrentToken();
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
        Integer size = parseInt(currentToken.getValue());
        if (getNextToken().getTokenType() != TokenType.CLOSE_SQUARE_BRACKET)
            throw new UnexpectedTokenException("]", currentToken);
        return new ArrayType(simpleType, size, currentToken.getTokenPosition());
    }

    private Identifier tryBuildIdentifier () throws Exception {
        Token startToken = currentToken;
        if (currentToken.getTokenType() != TokenType.IDENTIFIER)
            return null;
        getNextToken();
        return new Identifier(startToken.getValue(), startToken.getTokenPosition());
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
//        Number number = tryBuildNumber();
//        if (number != null)
//            return tryBuildCurrencyConversionBody(currencyAbbreviation, number);

//        BigDecimal bigDecimal = tryBuildBigDecimal();
//        if (bigDecimal != null)
//            return tryBuildCurrencyConversionBody(currencyAbbreviation, bigDecimal);
        if (currentToken.getTokenType() == TokenType.BIG_DECIMAL_NUMBER) {
            BigDecimal bigDecimal = new BigDecimal(currentToken.getValue());
            getNextToken();
            return tryBuildCurrencyConversionBody(currencyAbbreviation, bigDecimal);

        }

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
        Integer number = getIfNumber();
        if (number != null)
            return tryBuildCurrencyAssignmentBody(number);

//        BigDecimal bigDecimal = tryBuildBigDecimal();
//        if (bigDecimal != null)
//            return tryBuildCurrencyAssignmentBody(bigDecimal);
        if (currentToken.getTokenType() == TokenType.BIG_DECIMAL_NUMBER) {
            Token token = currentToken;
            getNextToken();
            return tryBuildCurrencyAssignmentBody(new BigDecimal(token.getValue()));
        }


        Identifier identifier = tryBuildIdentifier();
        if (identifier != null) {
            getNextToken();
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
        if (identifier != null) {
            return new Expression(identifier, currentToken.getTokenPosition());
        }
            //TRY BUILD FUNCTION CALL OR CURRENCY CONVERSION

        Integer number = getIfNumber();
        if (number != null) {
            return new Expression(number, currentToken.getTokenPosition());
        }

        if (currentToken.getTokenType() == TokenType.BIG_DECIMAL_NUMBER)
            return null;
        getNextToken();
        Currency currency = tryBuildCurrency(new BigDecimal(currentToken.getValue()));
        return new Expression(Objects.requireNonNullElseGet(currency, () ->
                new BigDecimal(currentToken.getValue())), currentToken.getTokenPosition());
    }

    public Currency tryBuildCurrency (BigDecimal bigDecimal) throws Exception {
        CurrencyAbbreviation currencyAbbreviation = tryBuildCurrencyAbbreviation();
        if (currencyAbbreviation == null)
            return null;
        getNextToken();
        return new Currency(bigDecimal, currencyAbbreviation, currentToken.getTokenPosition());
    }

    public Integer getIfNumber() throws Exception {
        if (currentToken.getTokenType() != TokenType.NUMBER)
            return null;
        int number = parseInt(currentToken.getValue());
        getNextToken();
        return number;
    }

//    public Number tryBuildNumber () throws Exception {
//        if (currentToken.getTokenType() != TokenType.NUMBER)
//            return null;
//        Token startToken = currentToken;
//        getNextToken();
//        return new Number(parseInt(startToken.getValue()), startToken.getTokenPosition());
//    }

    public ArrayElementReference tryBuildArrayElementReference (Identifier identifier) throws Exception {
        if (currentToken.getTokenType() != TokenType.OPEN_SQUARE_BRACKET)
            return null;
        Token nextToken = getNextToken();
        Identifier indexIdentifier = tryBuildIdentifier();
        if (indexIdentifier == null) {
            Integer number = getIfNumber();
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
        Identifier identifier = tryBuildIdentifier();
        if (identifier == null) {
            Expression expression = tryBuildExpression();
            if (expression == null)
                throw new UnexpectedTokenException("expression or function call", currentToken);
            getNextToken();
            return expression;
        }

        FunctionCall functionCall = tryBuildFunctionCall(identifier);
        if (functionCall != null) {
            getNextToken();
            return functionCall;
        }
        return new Expression(identifier, currentToken.getTokenPosition());
//        Expression expression = tryBuildExpression();
//        if (expression != null)
//            return expression;
//        getNextToken();
//        Identifier identifier = tryBuildIdentifier();
//        if (identifier == null)
//            return null;
//        FunctionCall functionCall = tryBuildFunctionCall(identifier);
//        return functionCall;
    }

    public SimpleExpression tryBuildSimpleExpression () throws Exception {
        if (currentToken.getTokenType() == TokenType.EXCLAMATION_MARK) {
            getNextToken();
            Object simpleExpressionContent = tryBuildSimpleExpressionContent();
            if (simpleExpressionContent == null)
                throw new UnexpectedTokenException("expression", currentToken);
            return new SimpleExpression(true, simpleExpressionContent, currentToken.getTokenPosition());
        } else {
            Object simpleExpressionContent = tryBuildSimpleExpressionContent();
            if (simpleExpressionContent == null)
                return null;
            return new SimpleExpression(false, simpleExpressionContent, currentToken.getTokenPosition());
        }
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
        if (currentToken.getTokenType() == TokenType.AND) {
            getNextToken();
            IfExpression ifExpression = tryBuildIfExpression();
            if (ifExpression == null)
                throw new UnexpectedTokenException("expression", currentToken);
            return new IfExpression(simpleOrComplexExpression, Optional.ofNullable(ifExpression), tokenPosition);
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

    public VariableDeclaration tryBuildVariableDeclaration() throws Exception {
        Type type = tryBuildType();
        if (type == null)
            return null;
        Identifier identifier = tryBuildIdentifier();
        if (identifier == null)
            throw new UnexpectedTokenException("identifier", currentToken);
        if (currentToken.getTokenType() == TokenType.ASSIGN) {
            getNextToken();
            VariableValueArray variableValueArray = tryBuildVariableValueArray();
            if (variableValueArray == null) {
                Expression expression = tryBuildExpression();
               if (expression == null)
                   throw new UnexpectedTokenException("expression or variable value", currentToken);
                if (currentToken.getTokenType() == TokenType.SEMI_COLON) {
                    getNextToken();
                    return new VariableDeclaration(type, identifier, expression, currentToken.getTokenPosition());
                }
            }
            if (currentToken.getTokenType() == TokenType.SEMI_COLON) {
                getNextToken();
                return new VariableDeclaration(type, identifier, variableValueArray, currentToken.getTokenPosition());
            }
        } else {
            if (currentToken.getTokenType() == TokenType.SEMI_COLON)
                return new VariableDeclaration(type, identifier, null, currentToken.getTokenPosition());
        }
        throw new UnexpectedTokenException(";", currentToken);
    }

    public ArgumentDeclaration tryBuildArgumentDeclaration() throws Exception {
        Type type = tryBuildType();
        if (type == null)
            return null;
        Identifier identifier = tryBuildIdentifier();
        if (identifier == null)
            throw new UnexpectedTokenException("identifier", currentToken);
        if (currentToken.getTokenType() == TokenType.ASSIGN) {
            getNextToken();
            ArgumentValue argumentValue = tryBuildArgumentValue();
            if (argumentValue == null)
                throw new UnexpectedTokenException("expression or variable value", currentToken);
            return new ArgumentDeclaration(type, identifier, argumentValue, currentToken.getTokenPosition());
            }
        else
            return new ArgumentDeclaration(type, identifier, null, currentToken.getTokenPosition());
    }

    public VariableDefinition tryBuildVariableDefinitionValue (Object name) throws Exception {
        if (currentToken.getTokenType() != TokenType.ASSIGN)
            throw new UnexpectedTokenException("=", currentToken);
        getNextToken();
        VariableValueArray variableValueArray = tryBuildVariableValueArray();
        if (variableValueArray != null) {
            if (currentToken.getTokenType() == TokenType.SEMI_COLON) {
                getNextToken();
                return new VariableDefinition(name, variableValueArray, currentToken.getTokenPosition());
            }
            throw new UnexpectedTokenException(";", currentToken);
        }

        Expression expression = tryBuildExpression();
        if (expression == null)
            throw new UnexpectedTokenException("expression", currentToken);
        if (currentToken.getTokenType() == TokenType.SEMI_COLON) {
            getNextToken();
            return new VariableDefinition(name, expression, currentToken.getTokenPosition());
        }
        throw new UnexpectedTokenException(";", currentToken);
    }

    public VariableDefinition tryBuildVariableDefinition (Identifier identifier) throws Exception {
//        VariableValueArray variableValueArray = tryBuildVariableValueArray();
        ArrayElementReference arrayElementReference = tryBuildArrayElementReference(identifier);
        if (arrayElementReference == null)
            getNextToken();
        return tryBuildVariableDefinitionValue(Objects.requireNonNullElse(arrayElementReference, identifier));
    }

    public ArgumentValue tryBuildArgumentValue() throws Exception {
        Integer number = getIfNumber();
        if (number != null)
            return new ArgumentValue(number, currentToken.getTokenPosition());
        if (currentToken.getTokenType() == TokenType.CONST_STRING)
            return new ArgumentValue(currentToken.getValue(), currentToken.getTokenPosition());
        if (currentToken.getTokenType() == TokenType.BOOLEAN_VALUE)
            return new ArgumentValue(currentToken.getValue(), currentToken.getTokenPosition());
        if (currentToken.getTokenType() == TokenType.BIG_DECIMAL_NUMBER)
            return new ArgumentValue(new BigDecimal(currentToken.getValue()), currentToken.getTokenPosition());
//        BigDecimal bigDecimal = tryBuildBigDecimal();
//        if (bigDecimal != null)
//            return new ArgumentValue(bigDecimal, currentToken.getTokenPosition());
        CurrencyConversion currencyConversion = tryBuildCurrencyConversion();
        if (currencyConversion != null)
            return new ArgumentValue(currencyConversion, currentToken.getTokenPosition());
        CurrencyAssignment currencyAssignment = tryBuildCurrencyAssignment();
        if (currencyAssignment != null)
            return new ArgumentValue(currencyAssignment, currentToken.getTokenPosition());
        return null;
    }

    public VariableValue tryBuildVariableValue() throws Exception {
        ArgumentValue argumentValue = tryBuildArgumentValue();
        if (argumentValue != null) {
            return new VariableValue(argumentValue, currentToken.getTokenPosition());
        }

        Identifier identifier = tryBuildIdentifier();
        if (identifier == null)
            return null;
        ArrayElementReference arrayElementReference = tryBuildArrayElementReference(identifier);
        if (arrayElementReference != null) {
            getNextToken();
            return new VariableValue(arrayElementReference, currentToken.getTokenPosition());
        }

        FunctionCall functionCall = tryBuildFunctionCall(identifier);
        if (functionCall == null)
            throw new UnexpectedTokenException("[, ) or ...", currentToken);
        getNextToken();
        return new VariableValue(functionCall, currentToken.getTokenPosition());

    }

    private ArrayList<Object> tryBuildFunctionCallArguments () throws Exception {
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
            if (currentToken.getTokenType() == TokenType.COMMA
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
            if (currentToken.getTokenType() == TokenType.COMMA
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
        if (!currentToken.getValue().equals("if"))
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
        Instruction instruction;
        ArrayList<Instruction> ifBody = new ArrayList<>();
        while((instruction = tryBuildInstruction()) != null)
            ifBody.add(instruction);
        if (currentToken.getTokenType() != TokenType.CLOSE_CURLY_BRACKET)
            throw new UnexpectedTokenException("}", currentToken);
        if (!getNextToken().getValue().equals("else"))
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
        ArrayList<Instruction> elseBody = new ArrayList<>();
        while((instruction = tryBuildInstruction()) != null)
            elseBody.add(instruction);
        if (currentToken.getTokenType() != TokenType.CLOSE_CURLY_BRACKET)
            throw new UnexpectedTokenException("}", currentToken);
        return new IfStatement(condition, ifBody, elseBody, Optional.of(ifStatement));
    }

    public ReturnExpression tryBuildReturnExpression() throws Exception {
        if (currentToken.getTokenType() != TokenType.RETURN)
            return null;
        getNextToken();
        VariableValueArray variableValueArray = tryBuildVariableValueArray();
        if (variableValueArray != null)
            return new ReturnExpression(variableValueArray, currentToken.getTokenPosition());
        Expression expression = tryBuildExpression();
        if (expression == null)
            throw new UnexpectedTokenException("variable value or expression", currentToken);
        if (currentToken.getTokenType() != TokenType.SEMI_COLON)
            throw new UnexpectedTokenException(";", currentToken);
        getNextToken();
        return new ReturnExpression(expression, currentToken.getTokenPosition());
    }

    public Instruction tryBuildInstruction() throws Exception {
        IfStatement ifStatement = tryBuildIfStatement();
        if (ifStatement != null)
            return new Instruction(ifStatement, currentToken.getTokenPosition());
        ForStatement forStatement = tryBuildForStatement();
        if (forStatement != null)
            return new Instruction(forStatement, currentToken.getTokenPosition());
        VariableDeclaration variableDeclaration = tryBuildVariableDeclaration();
        if (variableDeclaration != null)
            return new Instruction(variableDeclaration, currentToken.getTokenPosition());
        ReturnExpression returnExpression = tryBuildReturnExpression();
        if (returnExpression != null)
            return new Instruction(returnExpression, currentToken.getTokenPosition());

        Identifier identifier = tryBuildIdentifier();
        if (identifier == null)
            return null;
        FunctionCall functionCall = tryBuildFunctionCall(identifier);
        if (functionCall != null)
            return new Instruction(functionCall, currentToken.getTokenPosition());
        VariableDefinition variableDefinition = tryBuildVariableDefinition(identifier);
        if (variableDefinition != null)
            return new Instruction(variableDefinition, currentToken.getTokenPosition());
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
        Instruction instruction;
        ArrayList<Instruction> instructions = new ArrayList<>();
        while((instruction = tryBuildInstruction()) != null)
            instructions.add(instruction);
        if (currentToken.getTokenType() != TokenType.CLOSE_CURLY_BRACKET)
            throw new UnexpectedTokenException("{", currentToken);
        return new ForStatement(forExpression, instructions);
    }

    private Object tryBuildReturnType() throws Exception {
        if (currentToken.getTokenType() != TokenType.VOID)
            return tryBuildType();
        getNextToken();
        return currentToken.getValue();
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

    private ArrayList<Instruction> tryBuildFunctionDeclarationBody() throws Exception {
        if (getNextToken().getTokenType() != TokenType.OPEN_CURLY_BRACKET)
            throw new UnexpectedTokenException("{", currentToken);
        if (getNextToken().getTokenType() == TokenType.CLOSE_CURLY_BRACKET)
            return new ArrayList<>();
        Instruction instruction;
        ArrayList<Instruction> instructions = new ArrayList<>();
        while((instruction = tryBuildInstruction()) != null)
            instructions.add(instruction);
        if (getNextToken().getTokenType() == TokenType.CLOSE_CURLY_BRACKET)
            return instructions;
        throw new UnexpectedTokenException("}", currentToken);
    }

    public FunctionDeclaration tryBuildFunctionDeclaration() throws Exception {
        Token startToken = currentToken;
        Object returnType = tryBuildReturnType();
        if (returnType == null)
            return null;
        Identifier identifier = tryBuildIdentifier();
        if (identifier == null)
            throw new UnexpectedTokenException("identifier", currentToken);
        if (currentToken.getTokenType() != TokenType.OPEN_ROUND_BRACKET)
            throw new UnexpectedTokenException("(", currentToken);
        if (getNextToken().getTokenType() == TokenType.CLOSE_ROUND_BRACKET) {
            ArrayList<Instruction> instructions = tryBuildFunctionDeclarationBody();
            return new FunctionDeclaration(returnType, identifier, new ArrayList<>(), instructions, currentToken.getTokenPosition());
        }
        ArrayList<ArgumentDeclaration> argumentDeclarations = tryBuildFunctionDeclarationArguments();
        ArrayList<Instruction> instructions = tryBuildFunctionDeclarationBody();
        return new FunctionDeclaration(returnType, identifier, argumentDeclarations, instructions, currentToken.getTokenPosition());
    }
}
