package scanner;

import java.io.FileReader;
import java.io.IOException;

public class Scanner {
    private FileReader fileReader;
    private Token currentToken;
    private int character;

    public Token getCurrentToken() {
        return currentToken;
    }

    public Scanner (FileReader fileReader) {
        this.fileReader = fileReader;

        try {
            this.character = this.fileReader.read();
        } catch (IOException e) {}

        next();
    }

    public void next(){//każdy while sprawdzanie maxymalnej długości
        try {
            while (Character.isWhitespace((char)this.character)) {
                this.character = this.fileReader.read();

            }

            if (this.character==-1) {
                this.currentToken = new Token(TokenType.END_OF_FILE, "");
                return;
            }

            if (Character.isLetter((char)this.character)) {
                String identifier = "";
                identifier += (char)this.character;
                while ((this.character=this.fileReader.read()) != -1 && (Character.isLetter((char)this.character) || Character.isDigit((char)this.character) || (char)this.character == '_')) {
                    identifier += (char)character;
                }
                this.currentToken = new Token(TokenType.IDENTIFIER, identifier);
                return;
            }

            if (Character.isDigit((char)this.character)) {
                boolean isInteger = true;
                String identifier = "";
                identifier += (char)this.character;
                while((this.character=this.fileReader.read()) != -1) {
                    if (Character.isDigit((char)this.character)) {
                        identifier += (char)character;
                    } else if ((char)this.character == '.') {
                        identifier += (char)character;
                        isInteger = false;
                    }
                    else {
                        break;
                    }
                }
                if (isInteger) {
                    this.currentToken = new Token(TokenType.NUMBER, identifier);
                } else {
                    this.currentToken = new Token(TokenType.BIG_INTEGER, identifier);
                }
                return;
            }

//            if ((char)this.character == '&') {
//                if((this.character=this.fileReader.read()) != -1) {
//                    try {
//                        if ((char)this.character == '&') {
//                            this.character=this.fileReader.read();
//                            this.currentToken = new Token(TokenType.AND, "&&");
//                            return;
//                        } else throw new Exception();
//                    } catch (Exception e) {
//                        System.out.println("No such smth, only &&");
//
//                    }
//                }
//            }

//            if ((char)this.character == '|') {
//                if((this.character=this.fileReader.read()) != -1) {
//                    try {
//                        if ((char)this.character == '|') {
//                            this.character=this.fileReader.read();
//                            this.currentToken = new Token(TokenType.OR, "||");
//                            return;
//                        } else throw new Exception();
//                    } catch (Exception e) {
//                        System.out.println("No such smth, only ||");
//                    }
//
//                }
//            }

            switch ((char)this.character) {
                case '&':
                    if((this.character=this.fileReader.read()) != -1) {
                        try {
                            if ((char)this.character == '&') {
                                this.currentToken = new Token(TokenType.AND, "&&");
                                return;
                            } else throw new Exception();
                        } catch (Exception e) {
                            System.out.println("No such smth, only &&");
                            this.currentToken = new Token(TokenType.UNKNOWN, null);
                            return;
                        }
                    }
                    break;
                case '|':
                    if((this.character=this.fileReader.read()) != -1) {
                        try {
                            if ((char)this.character == '|') {
                                this.currentToken = new Token(TokenType.OR, "||");
                                return;
                            } else throw new Exception();
                        } catch (Exception e) {
                            System.out.println("No such smth, only ||");
                            this.currentToken = new Token(TokenType.UNKNOWN, null);
                            return;
                        }
                    }
                    break;
                case '{':
                    this.currentToken = new Token(TokenType.OPEN_CURLY_BRACKET, "{");
                    break;
                case '}':
                    this.currentToken = new Token(TokenType.CLOSE_CURLY_BRACKET, "}");
                    break;
                case '(':
                    this.currentToken = new Token(TokenType.OPEN_ROUND_BRACKET, "(");
                    break;
                case ')':
                    this.currentToken = new Token(TokenType.CLOSE_ROUND_BRACKET, ")");
                    break;
                case '[':
                    this.currentToken = new Token(TokenType.OPEN_SQUARE_BRACKET, "[");
                    break;
                case ']':
                    this.currentToken = new Token(TokenType.CLOSE_SQUARE_BRACKET, "]");
                    break;
                case '=':
                    this.currentToken = new Token(TokenType.EQUALS, "=");
                    break;
                case ',':
                    this.currentToken = new Token(TokenType.COMMA, ",");
                    break;
                case ';':
                    this.currentToken = new Token(TokenType.SEMI_COLON, ";");
                    break;
                case '!':
                    this.currentToken = new Token(TokenType.EXCLAMATION_MARK, "!");
                    break;
                case '"':
                    //while
                    this.currentToken = new Token(TokenType.QUATATION_MARKS, "\"");
                    break;
                case '.':
                    this.currentToken = new Token(TokenType.POINT, ".");
                    break;

                //comparisons
                case '<': //<=
                    this.currentToken = new Token(TokenType.LESS_THAN, "<");
                    break;
                case '>':
                    this.currentToken = new Token(TokenType.MORE_THAN, ">");
                    break;

                //math operations
                case '+':
                    this.currentToken = new Token(TokenType.PLUS, "+");
                    break;
                case '-':
                    this.currentToken = new Token(TokenType.MINUS, "-");
                    break;
                case '*':
                    this.currentToken = new Token(TokenType.MULTIPLY, "*");
                    break;
                case '/':
                    this.currentToken = new Token(TokenType.DIVIDE, "/");
                    break;

                default:
                    this.currentToken = new Token(TokenType.UNKNOWN, "");

            }
            this.character=this.fileReader.read();
        } catch (IOException e) {

        }
    }
}
