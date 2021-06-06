package parser;

import parser.basic.*;
import parser.instruction.*;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Integer.parseInt;

public class Parser {
    private final IScanner scanner;
    private Token currentToken;

    public Parser(IScanner scanner) {
        this.scanner = scanner;
        this.currentToken = scanner.getCurrentToken();
    }

    public Program parse() throws Exception {
        return tryBuildProgram();
    }

    private Token getNextToken() throws Exception {
        scanner.next();
        this.currentToken = scanner.getCurrentToken();
        if(currentToken.getTokenType() == TokenType.COMMENT)
            return getNextToken();
        return this.currentToken;
    }

    public boolean tryBuildImportOrFunctionDeclaration(List<Import> imports,
                                                       List<FunctionDeclaration> functionDeclarations) throws Exception {
        Import _import = tryBuildImport();
        if (_import != null) {
            imports.add(_import);
            return true;
        }
        FunctionDeclaration functionDeclaration = tryBuildFunctionDeclaration();
        if (functionDeclaration == null)
            return false;
        functionDeclarations.add(functionDeclaration);
        return true;
    }

    public Program tryBuildProgram() throws Exception {
        List<Import> imports = new ArrayList<>();
        List<FunctionDeclaration> functionDeclarations = new ArrayList<>();
        while (tryBuildImportOrFunctionDeclaration(imports, functionDeclarations));
        return new Program(imports, functionDeclarations, currentToken.getTokenPosition());
    }

    public Import tryBuildImport() throws Exception {
        if (currentToken.getTokenType() != TokenType.IMPORT)
            return null;
        TokenPosition startTokenPosition = currentToken.getTokenPosition();
        if (getNextToken().getTokenType() != TokenType.CONST_STRING)
            throw new UnexpectedTokenException("string constant", currentToken);
        String fileName = currentToken.getValue();
        if (getNextToken().getTokenType() != TokenType.SEMI_COLON)
            throw new UnexpectedTokenException(";", currentToken);
        getNextToken();
        return new Import(fileName, startTokenPosition);
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
            getNextToken();
            return new ArrayType(simpleType, null, currentToken.getTokenPosition());
        }
        Integer size = parseInt(currentToken.getValue());
        if (getNextToken().getTokenType() != TokenType.CLOSE_SQUARE_BRACKET)
            throw new UnexpectedTokenException("]", currentToken);
        getNextToken();
        return new ArrayType(simpleType, size, currentToken.getTokenPosition());
    }

    public Identifier tryBuildIdentifier () throws Exception {
        Token startToken = currentToken;
        if (currentToken.getTokenType() != TokenType.IDENTIFIER)
            return null;
        getNextToken();
        return new Identifier(startToken.getValue(), startToken.getTokenPosition());
    }

    public CurrencyAbbreviation tryBuildCurrencyAbbreviation () throws Exception {
        if (this.currentToken.getTokenType() == TokenType.IDENTIFIER && currentToken.getValue().length() != 3)
            return null;
        StringBuilder abbreviation = new StringBuilder();
        for (int i = 0; i < currentToken.getValue().length(); i++) {
            if (currentToken.getValue().charAt(i) < 'A' || currentToken.getValue().charAt(i) > 'Z')
                return null;
            abbreviation.append(currentToken.getValue().charAt(i));
        }
        getNextToken();
        return new CurrencyAbbreviation(abbreviation.toString(), currentToken.getTokenPosition());
    }

    public CurrencyConversion tryBuildCurrencyConversion() throws Exception {
        CurrencyAbbreviation currencyAbbreviation = tryBuildCurrencyAbbreviation();
        if (currencyAbbreviation == null)
            return null;
        if (currentToken.getTokenType() != TokenType.OPEN_ROUND_BRACKET)
            // ??????
            return null;
        getNextToken();
        Expression expression = tryBuildExpression();
        if (expression == null)
            throw new UnexpectedTokenException("expression", currentToken);
        if (currentToken.getTokenType() == TokenType.CLOSE_ROUND_BRACKET) {
            getNextToken();
            return new CurrencyConversion(currencyAbbreviation, expression, currentToken.getTokenPosition());
        }
        throw new UnexpectedTokenException(")", currentToken);
    }

    public CurrencyAssignment tryBuildCurrencyAssignment(Object content) throws Exception {
        CurrencyAbbreviation currencyAbbreviation = tryBuildCurrencyAbbreviation();
        if (currencyAbbreviation == null)
            return null;
        return new CurrencyAssignment(content, currencyAbbreviation, currentToken.getTokenPosition());
    }

    public Expression tryBuildExpression () throws Exception {
        Term term = tryBuildTerm();
        if (term == null)
            return null;
        if (!(currentToken.getTokenType() == TokenType.PLUS || currentToken.getTokenType() == TokenType.MINUS))
            return new Expression(term, currentToken.getTokenPosition());
        Expression.Operator operator = currentToken.getTokenType() == TokenType.PLUS ?
                Expression.Operator.PLUS : Expression.Operator.MINUS;
        getNextToken();
        Expression expression = tryBuildExpression();
        if (expression == null)
            throw new UnexpectedTokenException("expression", currentToken);
        return new Expression(term, Optional.of(operator), Optional.of(expression), currentToken.getTokenPosition());
    }

    public Integer getIfNumber() {
        if (currentToken.getTokenType() != TokenType.NUMBER)
            return null;
        return parseInt(currentToken.getValue());
    }

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
        if (currentToken.getTokenType() != TokenType.CLOSE_SQUARE_BRACKET)
            throw new UnexpectedTokenException("]", currentToken);
        getNextToken();
        return new ArrayElementReference(identifier, indexIdentifier, currentToken.getTokenPosition());
    }

    public Object tryBuildSimpleExpressionContent () throws Exception {
        Expression expression = tryBuildExpression();
        if (expression != null)
            return expression;

        Identifier identifier = tryBuildIdentifier();
        FunctionCall functionCall = tryBuildFunctionCall(identifier);
        if (functionCall != null) {
            getNextToken();
            return functionCall;
        }
        throw new UnexpectedTokenException("expression or function call", currentToken);
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
            return new Condition(isNegated, ifExpression, Optional.ofNullable(condition), tokenPosition);
        }
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
            if (currentToken.getTokenType() == TokenType.SEMI_COLON) {
                getNextToken();
                return new VariableDeclaration(type, identifier, null, currentToken.getTokenPosition());
            }
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
            throw new UnexpectedTokenException("expression or variable value", currentToken);
        if (currentToken.getTokenType() == TokenType.SEMI_COLON) {
            getNextToken();
            return new VariableDefinition(name, expression, currentToken.getTokenPosition());
        }
        throw new UnexpectedTokenException(";", currentToken);
    }

    public VariableDefinition tryBuildVariableDefinition (Identifier identifier) throws Exception {
        ArrayElementReference arrayElementReference = tryBuildArrayElementReference(identifier);
        return tryBuildVariableDefinitionValue(Objects.requireNonNullElse(arrayElementReference, identifier));
    }

    public ArgumentValue tryBuildArgumentValue() throws Exception {
        Integer number = getIfNumber();
        if (number != null) {
            getNextToken();
            CurrencyAssignment currencyAssignment = tryBuildCurrencyAssignment(number);
            if(currencyAssignment != null)
                return new ArgumentValue(currencyAssignment, currentToken.getTokenPosition());
            return new ArgumentValue(number, currentToken.getTokenPosition());
        }
        if (currentToken.getTokenType() == TokenType.BIG_DECIMAL_NUMBER) {
            BigDecimal bigDecimal = new BigDecimal(currentToken.getValue());
            getNextToken();
            CurrencyAssignment currencyAssignment = tryBuildCurrencyAssignment(bigDecimal);
            if(currencyAssignment != null)
                return new ArgumentValue(currencyAssignment, currentToken.getTokenPosition());
            return new ArgumentValue(bigDecimal, currentToken.getTokenPosition());
        }
        if (currentToken.getTokenType() == TokenType.CONST_STRING) {
            Token token = currentToken;
            getNextToken();
            return new ArgumentValue(token.getValue(), currentToken.getTokenPosition());
        }
        if (currentToken.getTokenType() == TokenType.BOOLEAN_VALUE) {
            Token token = currentToken;
            getNextToken();
            return new ArgumentValue(token.getValue(), currentToken.getTokenPosition());
        }
        CurrencyConversion currencyConversion = tryBuildCurrencyConversion();
        if (currencyConversion != null)
            return new ArgumentValue(currencyConversion, currentToken.getTokenPosition());
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
        if (arrayElementReference != null)
            return new VariableValue(arrayElementReference, currentToken.getTokenPosition());

        FunctionCall functionCall = tryBuildFunctionCall(identifier);
        return new VariableValue(Objects.requireNonNullElse(functionCall, identifier), currentToken.getTokenPosition());
    }

    private ArrayList<Object> tryBuildFunctionCallArguments () throws Exception {
        ArrayList<Object> arguments = new ArrayList<>();
        while (currentToken.getTokenType() != TokenType.CLOSE_ROUND_BRACKET) {
            getNextToken();
            if(currentToken.getTokenType() == TokenType.CLOSE_ROUND_BRACKET)
                break;
            VariableValueArray variableValueArray = tryBuildVariableValueArray();
            if (variableValueArray != null)
                arguments.add(variableValueArray);
            else {
                Object expression = tryBuildComplexExpression();
                if (expression == null)
                    throw new UnexpectedTokenException("expression", currentToken);
                arguments.add(expression);
            }
            if (currentToken.getTokenType() == TokenType.COMMA
                    || currentToken.getTokenType() == TokenType.CLOSE_ROUND_BRACKET)
                continue;
            throw new UnexpectedTokenException(", or )", currentToken);
        }
        getNextToken();
        return arguments;
    }

    public VariableValueArray tryBuildVariableValueArray() throws Exception {
        if (currentToken.getTokenType() != TokenType.OPEN_CURLY_BRACKET)
            return null;
        ArrayList<VariableValue> values = new ArrayList<>();
        while (currentToken.getTokenType() != TokenType.CLOSE_CURLY_BRACKET) {
            getNextToken();
            VariableValue value = tryBuildVariableValue();
            if (value == null)
                throw new UnexpectedTokenException("identifier", currentToken);
            values.add(value);
            if (currentToken.getTokenType() == TokenType.COMMA
                    || currentToken.getTokenType() == TokenType.CLOSE_CURLY_BRACKET)
                continue;
            throw new UnexpectedTokenException(", or )", currentToken);
        }
        getNextToken();
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

    public Term tryBuildTerm() throws Exception {
        Factor factor = tryBuildFactor();
        if (factor == null)
            return null;
        if (!(currentToken.getTokenType() == TokenType.MULTIPLY || currentToken.getTokenType() == TokenType.DIVIDE))
            return new Term(factor, currentToken.getTokenPosition());
        Term.Operator operator = currentToken.getTokenType() == TokenType.MULTIPLY ?
                Term.Operator.MULTIPLY :Term.Operator.DIVIDE;
        getNextToken();
        Term term = tryBuildTerm();
        if (term == null)
            throw new UnexpectedTokenException("term", currentToken);
        return new Term(factor, Optional.of(operator), Optional.of(term), currentToken.getTokenPosition());
    }

    public Factor tryBuildFactor() throws Exception {
       VariableValue variableValue = tryBuildVariableValue();
       if (variableValue != null)
           return new Factor(variableValue, currentToken.getTokenPosition());
       if (currentToken.getTokenType() != TokenType.OPEN_ROUND_BRACKET)
           return null;
       Expression expression = tryBuildExpression();
       if (expression == null)
           throw new UnexpectedTokenException("expression", currentToken);
       if (currentToken.getTokenType() != TokenType.CLOSE_CURLY_BRACKET)
            throw new UnexpectedTokenException(")", currentToken);
       getNextToken();
       return new Factor(expression, currentToken.getTokenPosition());
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
        if (functionCall != null) {
            if (currentToken.getTokenType() != TokenType.SEMI_COLON)
                throw new UnexpectedTokenException(";", currentToken);
            getNextToken();
            return new Instruction(functionCall, currentToken.getTokenPosition());
        }
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
        getNextToken();
        Identifier identifier = tryBuildIdentifier();
        if(identifier == null)
            return new ForExpression(variableDeclaration,complexExpression, null);
        VariableDefinition variableDefinition = tryBuildVariableDefinition(identifier);
        if (variableDefinition == null)
            throw new UnexpectedTokenException("expression", currentToken);
        return new ForExpression(variableDeclaration,complexExpression, variableDefinition);
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
        getNextToken();
        Instruction instruction;
        ArrayList<Instruction> instructions = new ArrayList<>();
        while((instruction = tryBuildInstruction()) != null)
            instructions.add(instruction);
        if (currentToken.getTokenType() != TokenType.CLOSE_CURLY_BRACKET)
            throw new UnexpectedTokenException("}", currentToken);
        getNextToken();
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
        if (getNextToken().getTokenType() == TokenType.CLOSE_CURLY_BRACKET) {
            getNextToken();
            return new ArrayList<>();
        }
        Instruction instruction;
        ArrayList<Instruction> instructions = new ArrayList<>();
        while((instruction = tryBuildInstruction()) != null)
            instructions.add(instruction);
        if (currentToken.getTokenType() != TokenType.CLOSE_CURLY_BRACKET)
            throw new UnexpectedTokenException("}", currentToken);
        getNextToken();
        return instructions;
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
