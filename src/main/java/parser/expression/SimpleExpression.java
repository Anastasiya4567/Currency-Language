package parser.expression;

import scanner.TokenPosition;

public class SimpleExpression {
    private boolean negation;
    private Object content;
    private TokenPosition tokenPosition;

    public boolean getNegation() {
        return negation;
    }

    public Object getContent() {
        return content;
    }

    public SimpleExpression(boolean negation, Object content, TokenPosition tokenPosition) {
        this.negation = negation;
        this.content = content;
        this.tokenPosition = tokenPosition;
    }
}
