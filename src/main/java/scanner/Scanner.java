package scanner;

import java.io.Reader;
import java.io.IOException;

public class Scanner {
    private Reader fileReader;
    private Token currentToken;
    private int character;

    public Token getCurrentToken() {
        return currentToken;
    }

    public Scanner (Reader fileReader) {
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
                int length = 1;
                while ((this.character=this.fileReader.read()) != -1 && (Character.isLetter((char)this.character) || Character.isDigit((char)this.character) || (char)this.character == '_')) {
                    identifier += (char)character;
                    length++;
                    if (length > 100) {
                        //throw ex
                    }

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
                    else break;
                }
                if (isInteger)
                    this.currentToken = new Token(TokenType.NUMBER, identifier);
                else
                    this.currentToken = new Token(TokenType.BIG_INTEGER, identifier);
                return;
            }

            if ((char) character == '&') {
                if ((this.character = this.fileReader.read()) != -1) {
                    if ((char) this.character == '&') {
                        this.currentToken = new Token(TokenType.AND, "&&");
                        this.character = this.fileReader.read();
                        return;
                    }
                } throw new Exception("No such smth, only &&");

            }

            if ((char) character == '|') {
                if ((this.character = this.fileReader.read()) != -1) {
                    if ((char) this.character == '|') {
                        this.currentToken = new Token(TokenType.OR, "||");
                        this.character = this.fileReader.read();
                        return;
                    }
                } throw new Exception("No such smth, only ||");
            }

            if ((char) character == '"') {
                String quotation = "";
                while ((this.character = this.fileReader.read()) != -1) {
                    if ((char) this.character != '"')
                        quotation += (char) this.character;
                    else {
                        this.currentToken = new Token(TokenType.CONST_STRING, "\"" + quotation + "\"");
                        return;
                    }
                }
                throw new Exception("No close quotation const");
            }

            if ((char) character == '/') {
                if ((this.character = this.fileReader.read()) != -1) {
                    if ((char) this.character == '*') {
                        String comment = "";
                        while ((this.character = this.fileReader.read()) != -1) {
                            if ((char) this.character == '*') {
                                comment += (char) this.character;
                                if ((this.character = this.fileReader.read()) != -1 && (char)this.character == '/') {
                                    this.currentToken = new Token(TokenType.COMMENT, "/*" + comment + "/");
                                    this.character = this.fileReader.read();
                                    return;
                                }
                                comment += (char) this.character;
                                continue;
                            }
                            comment += (char) this.character;
                        }
                        throw new Exception("No close comment const");
                    } else {
                        this.currentToken = new Token(TokenType.DIVIDE, "/");
                        return;
                    }
                } else {
                    this.currentToken = new Token(TokenType.DIVIDE, "/");
                    return;
                }
            }

            switch ((char)this.character) {
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
                    this.currentToken = new Token(TokenType.IS, "=");
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

                default:
                    this.currentToken = new Token(TokenType.UNDEFINED, "");

            }
            this.character=this.fileReader.read();
        } catch (IOException e) {
            currentToken = new Token(TokenType.UNDEFINED, "");
//            throw;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            this.currentToken = new Token(TokenType.UNDEFINED, null);
            return;
        }
    }
}
