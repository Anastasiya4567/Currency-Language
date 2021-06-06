package parser.instruction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.Instruction;
import parser.expression.ForExpression;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
public class ForStatement {
    private ForExpression forExpression;
    private ArrayList<Instruction> instructions;
}
