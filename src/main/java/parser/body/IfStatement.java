package parser.body;

import lombok.AllArgsConstructor;
import lombok.Getter;
import parser.Instruction;
import parser.Condition;

import java.util.ArrayList;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class IfStatement {
    private Condition condition;
    private ArrayList<Instruction> ifBody;
    private ArrayList<Instruction> elseBody;
    private Optional<IfStatement> ifStatement;
}
