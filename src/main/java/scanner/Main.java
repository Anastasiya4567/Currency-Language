package scanner;

import java.io.*;

public class Main {

    public static void main (String args[]) throws FileNotFoundException {
        FileReader fileReader;

        try {
//            BufferedReader inputStream = new BufferedReader(new FileReader("xanadu.txt"));
//            InputStream in = new FileInputStream("src/main/java/scanner/CProgram.txt");
//            Reader reader = new InputStreamReader(in, "US-ASCII");
////            vs
            fileReader = new FileReader("src/main/java/scanner/CProgram.txt");

            Scanner scanner = new Scanner(fileReader);
            while(scanner.getCurrentToken().getTokenType() != TokenType.END_OF_FILE) {
                System.out.println(scanner.getCurrentToken().getValue());
                scanner.next();
            }
        } catch (FileNotFoundException er) {
            System.out.println("No file found or unsupported encoding occurred");
        }
    }
}
