package interpreter;

import com.google.gson.Gson;
import com.google.gson.internal.Pair;
import parser.*;
import parser.basic.ArgumentValue;
import parser.basic.Identifier;
import parser.basic.VariableValue;
import parser.basic.VariableValueArray;
import parser.currency.CurrencyAbbreviation;
import parser.currency.CurrencyAssignment;
import parser.currency.CurrencyConversion;
import parser.expression.*;
import parser.instruction.*;
import parser.type.ArrayType;
import parser.type.SimpleType;
import parser.type.Type;
import scanner.token.TokenPosition;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Interpreter {
    private List<MyCurrency> myCurrencies;
    private final Program program;

    public Interpreter(Program program) {
        this.program = program;
        Reader file;

        try {
            file = Files.newBufferedReader(Paths.get("src/main/java/main/Currencies.txt"));
            Gson gson = new Gson();
            this.myCurrencies = Arrays.asList(gson.fromJson(file, MyCurrency[].class));

        } catch (FileNotFoundException er) {
            System.out.println("No such file found");
        } catch (IOException e) {
            System.out.println("Input/output error");
        }
    }

    public void interpret() throws Exception {
        for (FunctionDeclaration functionDeclaration : program.getFunctionDeclaration()) {
            if (functionDeclaration.getFunctionName().getValue().equals("main")) {
                List<Object> arguments = new ArrayList<>();
                interpretFunction(functionDeclaration, arguments);
                return;
            }
        }
        throw new Exception("Main not defined");
    }

    public void declareNewVariable(Map<String, Pair<Type, Object>> localVariables, Object variable, String name, Type type) throws Exception {
        if(localVariables.containsKey(name))
            throw new Exception(); //variable already declared
        localVariables.put(name, new Pair<>(type, variable));
    }

    public Object getVariableValue(ArrayList<Map<String, Pair<Type, Object>>> localVariables, String name) throws Exception {
        for(int i = localVariables.size()-1; i >= 0; --i) {
            Map<String, Pair<Type, Object>> localScope = localVariables.get(i);
            if(localScope.containsKey(name))
                return localScope.get(name).second;
        }
        throw new Exception(); //unknown variable
    }


    public void setVariableValue(ArrayList<Map<String, Pair<Type, Object>>> localVariables, Object variable, String name) throws Exception {
        for(int i = localVariables.size()-1; i >= 0; --i) {
            Map<String, Pair<Type, Object>> localScope = localVariables.get(i);
            if(localScope.containsKey(name))
                localScope.put(name, new Pair<>(localScope.get(name).first, variable));
        }
        throw new Exception(); //unknown variable
    }

    public Object evaluateFactor(ArrayList<Map<String, Pair<Type, Object>>> localVariables, Factor factor) throws Exception {
        if (factor.getValue() instanceof VariableValue) {
            return evaluateVariableValue(localVariables, (VariableValue) factor.getValue());
        } else if (factor.getValue() instanceof Expression) {
            return evaluateExpression(localVariables, (Expression) factor.getValue());
        }
        throw new Exception("Bad");
    }

    public Object evaluateTerm(ArrayList<Map<String, Pair<Type, Object>>> localVariables, Term term) throws Exception {
        Object factor = evaluateFactor(localVariables, term.getFactor());
        if(term.getTerm() == null)
            return factor;
        Object term2 = evaluateTerm(localVariables, term.getTerm().get());
        if(term.getOperator().get() == Term.Operator.MULTIPLY)
            return multiplyObjects(factor, term2);
        return divideObjects(factor, term2);
    }

    public Object multiplyObjects(Object factor, Object term) throws Exception {
        Pair<String,Integer> factorType = getObjectType(factor);
        Pair<String,Integer> termType = getObjectType(term);
        if(termType.second != -1 || factorType.second != -1)
            throw new Exception();
        String fType = factorType.first;
        String tType = termType.first;
        if(tType.equals(fType) && tType.equals("int"))
            return (Integer)factor * (Integer)term;
        if(tType.equals(fType) && tType.equals("BigDecimal") ||
                fType.equals("int") && tType.equals("BigDecimal") ||
                fType.equals("BigDecimal") && tType.equals("int"))
            return ((BigDecimal)factor).multiply((BigDecimal)term);
        if(fType.equals("Currency") && tType.equals("BigDecimal") ||
                fType.equals("Currency") && tType.equals("int")) {
            BigDecimal content = ((BigDecimal)((CurrencyAssignment) factor).getContent())
                    .multiply((BigDecimal)term);
            CurrencyAbbreviation currencyAbbreviation = ((CurrencyAssignment) factor).getCurrencyAbbreviation();
            return new CurrencyAssignment(content, currencyAbbreviation, new TokenPosition());
        }
        throw new Exception("Can't multiply variables of such types");
    }

    public Object divideObjects(Object factor, Object term) throws Exception {
        Pair<String,Integer> factorType = getObjectType(factor);
        Pair<String,Integer> termType = getObjectType(term);
        if(termType.second != -1 || factorType.second != -1)
            throw new Exception();
        String fType = factorType.first;
        String tType = termType.first;
        if(tType.equals(fType) && tType.equals("int"))
            return (Integer)factor / (Integer)term;
        if(tType.equals(fType) && tType.equals("BigDecimal") ||
                fType.equals("int") && tType.equals("BigDecimal") ||
                fType.equals("BigDecimal") && tType.equals("int"))
            return ((BigDecimal)factor).divide((BigDecimal)term);
        if(fType.equals("Currency") && tType.equals("BigDecimal") ||
                fType.equals("Currency") && tType.equals("int")) {
            BigDecimal content = ((BigDecimal)((CurrencyAssignment) factor).getContent())
                    .divide((BigDecimal)term);
            CurrencyAbbreviation currencyAbbreviation = ((CurrencyAssignment) factor).getCurrencyAbbreviation();
            return new CurrencyAssignment(content, currencyAbbreviation, new TokenPosition());
        }
        throw new Exception("Can't divide variables of such types");
    }

    public boolean isCurrencyPresentedInList(CurrencyAssignment currency) {
        for (MyCurrency myCurrency: this.myCurrencies) {
            if (currency.getCurrencyAbbreviation().getAbbreviation().equals(myCurrency.getAbbreviation()))
                return true;
        }
        return false;
    }

    public Object evaluateExpression(ArrayList<Map<String, Pair<Type, Object>>> localVariables, Expression expression) throws Exception {
        Object term = evaluateTerm(localVariables, expression.getTerm());
        if(expression.getExpression() == null)
            return term;
        Object expr = evaluateExpression(localVariables, expression.getExpression().get());
        if(expression.getOperator().get() == Expression.Operator.PLUS)
            return addObjects(term, expr);
        return subtractObjects(term, expr);
    }

    public Object addObjects(Object term, Object expression) throws Exception {
        Pair<String,Integer> termType = getObjectType(term);
        Pair<String,Integer> expressionType = getObjectType(expression);
        if(expressionType.second != -1 || termType.second != -1)
            throw new Exception();
        String terType = termType.first;
        String exprType = expressionType.first;
        if(exprType.equals(terType) && exprType.equals("String"))
            return ((String)term).concat(((String)expression));
        if(exprType.equals(terType) && exprType.equals("int"))
            return (Integer)term + (Integer)expression;
        if(exprType.equals(terType) && exprType.equals("BigDecimal") ||
                terType.equals("int") && exprType.equals("BigDecimal") ||
                terType.equals("BigDecimal") && exprType.equals("int"))
            return ((BigDecimal)term).add((BigDecimal)expression);
        if(exprType.equals(terType) && exprType.equals("Currency"))
            return addCurrencies((CurrencyAssignment)term, (CurrencyAssignment)expression);
        throw new Exception("Can't sum variables of such types");
    }

    public Object addCurrencies(CurrencyAssignment firstCurrency, CurrencyAssignment secondCurrency) throws Exception {
        if (firstCurrency.getCurrencyAbbreviation() == secondCurrency.getCurrencyAbbreviation()) {
            BigDecimal currenciesSum = ((BigDecimal) firstCurrency.getContent()).add((BigDecimal)secondCurrency.getContent());
            return new CurrencyAssignment(currenciesSum, firstCurrency.getCurrencyAbbreviation(), new TokenPosition());
        }

        if (!isCurrencyPresentedInList(firstCurrency))
            throw new Exception("No such currency defined");
        if (!isCurrencyPresentedInList(secondCurrency))
            throw new Exception("No such currency defined");

        BigDecimal rate = null;
        for (MyCurrency myCurrency: this.myCurrencies) {
            if (myCurrency.getAbbreviation().equals(firstCurrency.getCurrencyAbbreviation().getAbbreviation())) {
                rate = new BigDecimal(myCurrency.getValue());
            }
        }

        for (MyCurrency myCurrency: this.myCurrencies) {
            if (new BigDecimal(myCurrency.getValue()).equals(new BigDecimal(1))) {
                BigDecimal result = (new BigDecimal(myCurrency.getValue())
                        .multiply(((BigDecimal)secondCurrency.getContent()).multiply(rate)))
                        .subtract((BigDecimal)firstCurrency.getContent());
                return new CurrencyAssignment(result, firstCurrency.getCurrencyAbbreviation(), new TokenPosition());
            }
        }
        throw new Exception("No basic currency defined");
    }

    public Object subtractObjects(Object term, Object expression) throws Exception {
        Pair<String,Integer> termType = getObjectType(term);
        Pair<String,Integer> expressionType = getObjectType(expression);
        if(expressionType.second != -1 || termType.second != -1)
            throw new Exception();
        String terType = termType.first;
        String exprType = expressionType.first;
        if(exprType.equals(terType) && exprType.equals("int"))
            return (Integer)term - (Integer)expression;
        if(exprType.equals(terType) && exprType.equals("BigDecimal") ||
                terType.equals("int") && exprType.equals("BigDecimal") ||
                terType.equals("BigDecimal") && exprType.equals("int"))
            return ((BigDecimal)term).subtract((BigDecimal)expression);
        if(exprType.equals(terType) && exprType.equals("Currency"))
            return subtractCurrencies((CurrencyAssignment)term, (CurrencyAssignment)expression);
        throw new Exception("Can't subtract variables of such types");
    }

    public Object subtractCurrencies(CurrencyAssignment firstCurrency, CurrencyAssignment secondCurrency) throws Exception {
        if (firstCurrency.getCurrencyAbbreviation() == secondCurrency.getCurrencyAbbreviation()) {
            BigDecimal currenciesSum = ((BigDecimal) firstCurrency.getContent()).subtract((BigDecimal)secondCurrency.getContent());
            return new CurrencyAssignment(currenciesSum, firstCurrency.getCurrencyAbbreviation(), new TokenPosition());
        }

        if (!isCurrencyPresentedInList(firstCurrency))
            throw new Exception("No such currency defined");
        if (!isCurrencyPresentedInList(secondCurrency))
            throw new Exception("No such currency defined");

        BigDecimal rate = null;
        for (MyCurrency myCurrency: this.myCurrencies) {
            if (myCurrency.getAbbreviation().equals(firstCurrency.getCurrencyAbbreviation().getAbbreviation())) {
                rate = new BigDecimal(myCurrency.getValue());
            }
        }

        for (MyCurrency myCurrency: this.myCurrencies) {
            if (new BigDecimal(myCurrency.getValue()).equals(new BigDecimal(1))) {
                BigDecimal result = (new BigDecimal(myCurrency.getValue())
                        .multiply(((BigDecimal)secondCurrency.getContent()).multiply(rate)))
                        .add((BigDecimal)firstCurrency.getContent());
                return new CurrencyAssignment(result, firstCurrency.getCurrencyAbbreviation(), new TokenPosition());
            }
        }
        throw new Exception("No basic currency defined");
    }

    public Pair<String, Integer> evaluateSimpleType(SimpleType simpleType) throws Exception {
       if (simpleType.getType() != null)
           return new Pair<>(simpleType.getType(), -1);
       throw new Exception("Unknown type");
    }

    public Pair<String, Integer> evaluateArrayType(ArrayType arrayType) throws Exception {
        if (arrayType.getType() == null)
            throw new Exception("Unknown type");
        if (arrayType.getSize() != null)
            return new Pair<>(arrayType.getType().getType(), arrayType.getSize());
        return new Pair<>(arrayType.getType().getType(), 0);
    }

    public Pair<String, Integer> evaluateType(Type type) throws Exception {
        if (type instanceof SimpleType) {
            return evaluateSimpleType((SimpleType) type);
        } else if (type instanceof ArrayType) {
            return evaluateArrayType((ArrayType) type);
        }
        return null;
    }

    public Object evaluateArgumentValue(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                        ArgumentValue argumentValue) throws Exception {
        if (argumentValue.getValue() instanceof BigDecimal ||
                argumentValue.getValue() instanceof Integer ||
                argumentValue.getValue() instanceof String ||
                argumentValue.getValue() instanceof Boolean) {
            return argumentValue.getValue();
        } else if (argumentValue.getValue() instanceof CurrencyConversion) {
            evaluateCurrencyConversion(localVariables, (CurrencyConversion) argumentValue.getValue());
        } else if (argumentValue.getValue() instanceof CurrencyAssignment) {
            return (CurrencyAssignment) argumentValue.getValue();
        }
        throw new Exception();
    }

    public Object evaluateCurrencyConversion(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                             CurrencyConversion currencyConversion) throws Exception {
        Object object = evaluateExpression(localVariables, currencyConversion.getExpression());
        if(object instanceof Integer || object instanceof BigDecimal) {
            return new CurrencyAssignment(object, currencyConversion.getCurrencyAbbreviation(), null);
        }
        if(object instanceof CurrencyAssignment) {

            //CurrencyAssignment currencyAssignment = new CurrencyAssignment();
        }
        throw new Exception();
    }

    public Object evaluateArrayElementReference(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                                ArrayElementReference arrayElementReference) {
        return null;
    }

    public Object interpretFunctionCall(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                       FunctionCall functionCall) throws Exception {
        ArrayList<Object> arguments = new ArrayList<>();
        for (Object argument: functionCall.getArguments()) {
            arguments.add(evaluateVariableValueArrayOrComplexExpression(localVariables, argument));
        }
        String functionName = functionCall.getName().getValue();
        for(FunctionDeclaration functionDeclaration : program.getFunctionDeclaration()) {
            if(functionDeclaration.getFunctionName().getValue().equals(functionName)) {
                if(functionDeclaration.getArguments().size() != arguments.size())
                    throw new Exception("invalid argument number");
                for(int i = 0; i < arguments.size(); ++i) {
                    Pair<String, Integer> type1 = getObjectType(arguments.get(i));
                    Pair<String, Integer> type2 = evaluateType(functionDeclaration.getArguments().get(i).getType());
                    if(!type1.equals(type2))
                        throw new Exception("type mismatch");
                }
                return interpretFunction(functionDeclaration, arguments);
            }
        }
        if (!functionName.equals("print"))
            throw new Exception("unknown function name " + functionName);
        for (Object argument: arguments) {
            System.out.println(argument);
        }
        return null;
    }

    public Object evaluateVariableValue(ArrayList<Map<String, Pair<Type, Object>>> localVariables, VariableValue variableValue) throws Exception {
        if (variableValue.getValue() instanceof ArrayElementReference) {
            return evaluateArrayElementReference(localVariables, (ArrayElementReference) variableValue.getValue());
        } else if (variableValue.getValue() instanceof FunctionCall) {
            return interpretFunctionCall(localVariables, (FunctionCall) variableValue.getValue());
        } else if (variableValue.getValue() instanceof ArgumentValue) {
            return evaluateArgumentValue(localVariables, (ArgumentValue) variableValue.getValue());
        } else if (variableValue.getValue() instanceof Identifier) {
            return getVariableValue(localVariables, ((Identifier)variableValue.getValue()).getValue());
        }
        throw new Exception();
    }

    public Pair<String, Integer> getObjectType(Object type) throws Exception {
        if (type instanceof Boolean) {
            return new Pair<>("boolean", -1);
        } else if (type instanceof Integer) {
            return new Pair<>("int", -1);
        } else if (type instanceof BigDecimal) {
            return new Pair<>("BigDecimal", -1);
        } else if (type instanceof String) {
            return new Pair<>("String", -1);
        }
        throw new Exception("Unknown type");
    }

    public Pair<Type, Object> evaluateVariableValueArray(ArrayList<Map<String, Pair<Type, Object>>> localVariables, Type type, VariableValueArray variableValueArray) throws Exception
    {
        Pair<String, Integer> variableType = evaluateType(type);
        Object[] array = new Object[variableValueArray.getValues().size()];
        for (int i = 0; i < array.length; ++i) {
            Object objectType = evaluateVariableValue(localVariables, variableValueArray.getValues().get(i));
            Pair<String, Integer> typeValue = getObjectType(objectType);
            if(!variableType.first.equals(typeValue.first))
                throw new Exception("Incompatible types");
            array[i] = (variableValueArray.getValues().get(i));
        }
        return new Pair<>(type, array);
    }

    public Object evaluateVariableValueArrayOrExpression(
            ArrayList<Map<String, Pair<Type, Object>>> localVariables, Type type, Object object) throws Exception {
        if (object instanceof Expression) {
            return evaluateExpression(localVariables, (Expression)object);
        } else if (object instanceof VariableValueArray) {
            return evaluateVariableValueArray( localVariables, type, (VariableValueArray)object);
        } else {
            throw new Exception();
        }
    }

    public Object evaluateSimpleExpression(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                           SimpleExpression simpleExpression) throws Exception {
        if (simpleExpression.getContent() instanceof FunctionCall) {
            return interpretFunctionCall(localVariables, (FunctionCall) simpleExpression.getContent());
        } else if (simpleExpression.getContent() instanceof Expression) {
            return evaluateExpression(localVariables, (Expression) simpleExpression.getContent());
        }
        throw new Exception();
    }

    public void interpretIfExpression(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                      IfExpression ifExpression) throws Exception {
        if (ifExpression.getExpression() instanceof ComplexExpression) {
            evaluateComplexExpression(localVariables, (ComplexExpression) ifExpression.getExpression());
        } else if (ifExpression.getExpression() instanceof SimpleExpression) {
            evaluateSimpleExpression(localVariables, (SimpleExpression) ifExpression.getExpression());
        } else throw new Exception();

        if (!ifExpression.getIfExpression().isEmpty()) {
            interpretIfExpression(localVariables, ifExpression.getIfExpression().get());
        }

    }

    public void interpretCondition(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                   Condition condition) throws Exception {
        interpretIfExpression(localVariables, condition.getIfExpression());
    }

    public void interpretIfStatement(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                            IfStatement ifStatement) throws Exception {
        interpretCondition(localVariables, ifStatement.getCondition());
        if (!ifStatement.getIfStatement().isEmpty()) {
            interpretIfStatement(localVariables, ifStatement.getIfStatement().get());
        }
    }

    public void interpretForStatement(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                       ForStatement forStatement) throws Exception {
        for (Instruction instruction: forStatement.getInstructions()) {
            interpretInstruction(localVariables, instruction);
        }
    }

    public Object evaluateVariableValueArrayOrComplexExpression(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                                                Object object) throws Exception {
        if (object instanceof VariableValueArray) {
            //TODO
            return evaluateVariableValueArray(localVariables, null, (VariableValueArray) object);
        } else if (object instanceof ComplexExpression) {
            return evaluateComplexExpression(localVariables, (ComplexExpression)object);
        } else if (object instanceof SimpleExpression) {
            return evaluateSimpleExpression(localVariables, (SimpleExpression)object);
        } else throw new Exception();
    }

    public Object evaluateComplexExpression(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                            ComplexExpression complexExpression) {
        return null;
    }

    public void interpretVariableDefinition(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                              VariableDefinition variableDefinition) throws Exception {
        Object value = evaluateVariableValueArrayOrComplexExpression(localVariables, variableDefinition.getValue());
        setVariableValue(localVariables, value, ((Identifier)variableDefinition.getIdentifier()).getValue());
    }

    public Object interpretReturnExpression(ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                            ReturnExpression returnExpression) throws Exception {
        return evaluateVariableValueArrayOrExpression(localVariables, null, returnExpression.getReturnValue());
    }

    public Object interpretFunction(FunctionDeclaration functionDeclaration, List<Object> arguments) throws Exception {
        ArrayList<Map<String, Pair<Type, Object>>> localVariables = new ArrayList<>();
        localVariables.add(new HashMap<>());
        ArrayList<ArgumentDeclaration> argumentValues = functionDeclaration.getArguments();
        for(int i = 0; i < arguments.size(); ++i) {
            declareNewVariable(localVariables.get(0),arguments.get(i), argumentValues.get(i).getIdentifier().getValue(), argumentValues.get(i).getType());
        }
        for (Instruction instruction : functionDeclaration.getInstructions()) {
            interpretInstruction(localVariables, instruction);
        }
        if(functionDeclaration.getInstructions().size()>0) {
            Instruction instruction = functionDeclaration.getInstructions().get(functionDeclaration.getInstructions().size()-1);
            if(instruction.getBody() instanceof ReturnExpression) {
                ReturnExpression returnExpression = (ReturnExpression)instruction.getBody();
                return evaluateVariableValueArrayOrExpression(localVariables, null, returnExpression.getReturnValue());
            }
        }
        return null;
    }



    public void interpretInstruction (ArrayList<Map<String, Pair<Type, Object>>> localVariables,
                                      Instruction instruction) throws Exception {
        if(instruction.getBody() instanceof IfStatement) {
            localVariables.add(new HashMap<>());
            interpretIfStatement(localVariables, (IfStatement)instruction.getBody());
            localVariables.remove(localVariables.size()-1);
        } else if (instruction.getBody() instanceof ForStatement) {
            localVariables.add(new HashMap<>());
            interpretForStatement(localVariables, (ForStatement)instruction.getBody());
            localVariables.remove(localVariables.size()-1);
        } else if (instruction.getBody() instanceof VariableDeclaration) {
            VariableDeclaration variableDeclaration = (VariableDeclaration)instruction.getBody();
            Identifier name = variableDeclaration.getIdentifier();
            Object object = variableDeclaration.getValue();
            Type type = variableDeclaration.getType();

            Object variable = new Pair<>(type, null);
            if(object != null)
                variable = evaluateVariableValueArrayOrExpression(localVariables, type, object);
            declareNewVariable(localVariables.get(localVariables.size()-1), variable, name.getValue(), type);
        } else if (instruction.getBody() instanceof FunctionCall) {
            interpretFunctionCall(localVariables, (FunctionCall)instruction.getBody());
        } else if (instruction.getBody() instanceof VariableDefinition) {
            interpretVariableDefinition(localVariables, (VariableDefinition)instruction.getBody());
        }
        else if (instruction.getBody() instanceof ReturnExpression) {
            interpretReturnExpression(localVariables, (ReturnExpression)instruction.getBody());
        }
    }
}
