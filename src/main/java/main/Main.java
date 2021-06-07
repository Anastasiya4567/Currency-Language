package main;

import interpreter.Interpreter;
import parser.Parser;
import parser.Program;
import scanner.Scanner;

import java.io.*;

public class Main {

    public static void main (String[] args) throws Exception {
        FileReader fileReader;

        try {
            fileReader = new FileReader("src/main/java/main/CProgram.txt");

            Scanner scanner = new Scanner(fileReader);
            Parser parser = new Parser(scanner);
            Program program = parser.parse();
            Interpreter interpreter = new Interpreter(program);
            interpreter.interpret();

        } catch (FileNotFoundException er) {
            System.out.println("No such file found");
        } catch (IOException e) {
            System.out.println("Input/output error");
        }
    }
}
