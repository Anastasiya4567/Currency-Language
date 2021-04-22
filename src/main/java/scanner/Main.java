package scanner;

import java.io.*;

public class Main {

    public static void main (String[] args) {
        FileReader fileReader;

        try {
            fileReader = new FileReader("src/main/java/scanner/CProgram.txt");

            Scanner scanner = new Scanner(fileReader);
            while(scanner.getCurrentToken().getTokenType() != TokenType.END_OF_FILE) {
                System.out.println(scanner.getCurrentToken().getValue());
                scanner.next();
            }
        } catch (FileNotFoundException er) {
            System.out.println("No such file found");
        } catch (IOException e) {
            System.out.println("Input/output error");
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
