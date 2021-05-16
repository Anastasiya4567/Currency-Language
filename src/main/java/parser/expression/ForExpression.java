package parser.expression;

import parser.basic.Identifier;

public class ForExpression {
    private Identifier iteratorName;
    private Object startIteratorValue;
    private String relOperator;
    private Object finishIteratorValue;
    private String mathOperation;
    private Number mathOperationNumber;

    public ForExpression(Identifier iteratorName, Object startIteratorValue, String relOperator,
                         Object finishIteratorValue, String mathOperation, Number mathOperationNumber) {
        this.iteratorName = iteratorName;
        this.startIteratorValue = startIteratorValue;
        this.relOperator = relOperator;
        this.finishIteratorValue = finishIteratorValue;
        this.mathOperation = mathOperation;
        this.mathOperationNumber = mathOperationNumber;
    }

    public Identifier getIteratorName() {
        return iteratorName;
    }

    public Object getStartIteratorValue() {
        return startIteratorValue;
    }

    public String getRelOperator() {
        return relOperator;
    }

    public Object getFinishIteratorValue() {
        return finishIteratorValue;
    }

    public String getMathOperation() {
        return mathOperation;
    }

    public Number getMathOperationNumber() {
        return mathOperationNumber;
    }
}
