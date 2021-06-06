package interpreter;

import com.google.gson.Gson;
import parser.FunctionDeclaration;
import parser.Instruction;
import parser.Parser;
import parser.Program;
import scanner.Scanner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Interpreter {
    private List<MyCurrency> myCurrencies;
    private Program program;

    public Interpreter(Program program) {
        this.program = program;
        Reader file;

        try {
            file = Files.newBufferedReader(Paths.get("src/main/java/scanner/Currencies.txt"));
            Gson gson = new Gson();
            this.myCurrencies = Arrays.asList(gson.fromJson(file, MyCurrency[].class));

        } catch (FileNotFoundException er) {
            System.out.println("No such file found");
        } catch (IOException e) {
            System.out.println("Input/output error");
        }
    }

    public void interpret() throws Exception {
        for(FunctionDeclaration functionDeclaration : program.getFunctionDeclaration()) {
//            System.out.println(functionDeclaration.getFunctionName().getValue());
            if(functionDeclaration.getFunctionName().getValue().equals("main")) {
                List<Object> arguments = new ArrayList<>();
                interpretFunction(functionDeclaration, arguments);
                return;
            }
        }
        throw new Exception("Main not defined");
    }

    public Object interpretFunction(FunctionDeclaration functionDeclaration, List<Object> arguments) {
        ArrayList<Map<String, Object>> localVariables = new ArrayList<>();
        localVariables.add(new HashMap<String, Object>());
        int current_scope = 0;
        for(Instruction instruction : functionDeclaration.getInstructions()) {

        }
        return null;
    }
}
