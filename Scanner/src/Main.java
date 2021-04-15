package Scanner.src;

import Scanner.Scanner;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class Main {

    public static void main (String args[]) {
        FileReader fileReader;

        try {
            fileReader = new FileReader("Scanner/src/CProgram.txt");

            Scanner scanner = new Scanner(fileReader);
            while(scanner.getCurrentToken().getTokenType() != TokenType.END_OF_FILE) {
                System.out.println(scanner.getCurrentToken().getValue());
                scanner.next();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("No file found");
        }







    }
}
