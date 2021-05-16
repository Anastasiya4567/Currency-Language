package scanner;

import parser.Parser;

import java.io.*;

public class Main {

    public static void main (String[] args) throws Exception {
        FileReader fileReader;

        try {
            fileReader = new FileReader("src/main/java/scanner/CProgram.txt");

            Scanner scanner = new Scanner(fileReader);
            Parser parser = new Parser(scanner);
            parser.parse();

        } catch (FileNotFoundException er) {
            System.out.println("No such file found");
        } catch (IOException e) {
            System.out.println("Input/output error");
        }
    }
}
