package scanner;

import lombok.Getter;
import scanner.exception.BadTokenSyntaxException;
import scanner.token.Token;
import scanner.token.TokenPosition;
import scanner.token.TokenType;

import java.io.Reader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Scanner implements IScanner {
    private final Reader reader;
    private Token currentToken;
    private int character;
    private final static int MAX_NUMBER_OF_SPACES = 1000;
    private final static int MAX_IDENTIFIER_LENGTH = 200;
    private final static int MAX_NUMBER_LENGTH = 32;
    private final static int MAX_CONST_STRING_LENGTH = 10000;
    private final Map<String, TokenType> tokens = new HashMap<>();
    private final Map<String, TokenType> keyWords = new HashMap<>();

    private int line = 1, column = 0;

    private void buildMapOfTokens() {
        tokens.put("{", TokenType.OPEN_CURLY_BRACKET);
        tokens.put("}", TokenType.CLOSE_CURLY_BRACKET);
        tokens.put("[", TokenType.OPEN_SQUARE_BRACKET);
        tokens.put("]", TokenType.CLOSE_SQUARE_BRACKET);
        tokens.put("(", TokenType.OPEN_ROUND_BRACKET);
        tokens.put(")", TokenType.CLOSE_ROUND_BRACKET);
        tokens.put(",", TokenType.COMMA);
        tokens.put(";", TokenType.SEMI_COLON);
        tokens.put(".", TokenType.POINT);

        //math operations
        tokens.put("+", TokenType.PLUS);
        tokens.put("-", TokenType.MINUS);
        tokens.put("*", TokenType.MULTIPLY);
        tokens.put("/", TokenType.DIVIDE);
    }

    private void buildMapOfKeyWords() {
        keyWords.put("true", TokenType.BOOLEAN_VALUE);
        keyWords.put("false", TokenType.BOOLEAN_VALUE);
        keyWords.put("import", TokenType.IMPORT);
        keyWords.put("return", TokenType.RETURN);
        keyWords.put("if", TokenType.IF);
        keyWords.put("else", TokenType.ELSE);
        keyWords.put("for", TokenType.FOR);
        keyWords.put("int", TokenType.INT);
        keyWords.put("boolean", TokenType.BOOLEAN);
        keyWords.put("BigDecimal", TokenType.BIG_DECIMAL);
        keyWords.put("String", TokenType.STRING);
        keyWords.put("Currency", TokenType.CURRENCY);
        keyWords.put("void", TokenType.VOID);
        //keyWords.put("main", TokenType.MAIN_FUNCTION);
    }

    public Scanner(Reader fileReader) throws Exception {

        buildMapOfTokens();
        buildMapOfKeyWords();

        this.reader = fileReader;

        try {
            getNextCharacter();
        } catch (IOException e) {
            throw new Exception("Input/output error");
        }
        next();
    }

    private int getNextCharacter() throws IOException {
        this.character = reader.read();
        switch ((char) this.character) {
            case '\t' -> this.column += 4;
            case '\n' -> {
                this.line++;
                this.column = 0;
            }
            default -> this.column++;
        }
        return this.character;
    }

    private boolean ifNumber(int currentLine, int currentColumn) throws Exception {
        if (Character.isDigit((char) this.character)) {
            boolean isInteger = true;
            int numberOfDigits = 1;
            StringBuilder identifier = new StringBuilder("" + (char) this.character);
            if ((char) character == '0') {
                getNextCharacter();
                if (Character.isDigit((char) this.character))
                    throw new BadTokenSyntaxException("Bad format of number", currentLine, currentColumn);
                if ((char) this.character != '.') {
                    this.currentToken = new Token(TokenType.NUMBER, identifier.toString(),
                            new TokenPosition(currentLine, currentColumn));
                    return true;
                } else {
                    identifier.append((char) this.character);
                    isInteger = false;
                }
            }

            getNextCharacter();
            if (!isInteger && !Character.isDigit((char) this.character))
                throw new BadTokenSyntaxException("Bad format of number", currentLine, currentColumn);
            while (Character.isDigit((char) this.character) || (char) this.character == '.') {
                if (Character.isDigit((char) this.character)) {
                    identifier.append((char) character);
                    if (++numberOfDigits >= MAX_NUMBER_LENGTH)
                        throw new BadTokenSyntaxException("The length of number can't be so large", currentLine, currentColumn);
                }

                if ((char) this.character == '.') {
                    if (!isInteger)
                        throw new BadTokenSyntaxException("Bad format of number", currentLine, currentColumn);
                    else {
                        identifier.append((char) character);
                        getNextCharacter();
                        if (!Character.isDigit((char) this.character))
                            throw new BadTokenSyntaxException("Bad format of number", currentLine, currentColumn);
                        while (Character.isDigit((char) this.character)) {
                            identifier.append((char) character);
                            if (++numberOfDigits >= MAX_NUMBER_LENGTH)
                                throw new BadTokenSyntaxException("The length of number can't be so large", currentLine, currentColumn);
                            getNextCharacter();
                        }
                        this.currentToken = new Token(TokenType.BIG_DECIMAL_NUMBER, identifier.toString(),
                                new TokenPosition(currentLine, currentColumn));
                        return true;
                    }
                }
                getNextCharacter();
            }
            if (isInteger) {
                this.currentToken = new Token(TokenType.NUMBER, identifier.toString(),
                        new TokenPosition(currentLine, currentColumn));
                return true;
            }
            this.currentToken = new Token(TokenType.BIG_DECIMAL_NUMBER, identifier.toString(),
                        new TokenPosition(currentLine, currentColumn));
            return true;
        }
        return false;
    }

    private boolean buildComment(int currentLine, int currentColumn) throws Exception {
        StringBuilder comment = new StringBuilder();
        while (getNextCharacter() != -1) {
            if ((char) this.character == '*') {
                comment.append((char) this.character);
                if (getNextCharacter() != -1) {
                    if ((char) this.character == '/') {
                        this.currentToken = new Token(TokenType.COMMENT, "/*" + comment + "/",
                                new TokenPosition(currentLine, currentColumn));
                        getNextCharacter();
                        return true;
                    } else
                        comment.append((char) this.character);
                } else throw new BadTokenSyntaxException("Unclosed comment", currentLine, currentColumn);
            } else
                comment.append((char) this.character);
        }
        throw new BadTokenSyntaxException("Unclosed comment", currentLine, currentColumn);
    }

    private boolean ifComment(int currentLine, int currentColumn) throws Exception {
        if ((char) character != '/')
            return false;
        getNextCharacter();
        if ((char) this.character == '*')
            return this.buildComment(currentLine, currentColumn);
        else {
            this.currentToken = new Token(TokenType.DIVIDE, "/",
                    new TokenPosition(currentLine, currentColumn));
            return true;
        }
    }

    private boolean ifLogicalOperators(int currentLine, int currentColumn) throws Exception {
        if ((char) character == '&') {
            getNextCharacter();
            if ((char) this.character != '&')
                throw new BadTokenSyntaxException("Expected && but got &" + (char) this.character, currentLine, currentColumn);
            this.currentToken = new Token(TokenType.AND, "&&", new TokenPosition(currentLine, currentColumn));
            getNextCharacter();
            return true;
        }

        if ((char) character == '|') {
            getNextCharacter();
            if ((char) this.character != '|')
                throw new BadTokenSyntaxException("Expected || but got |" + (char)this.character, currentLine, currentColumn);
            this.currentToken = new Token(TokenType.OR, "||", new TokenPosition(currentLine, currentColumn));
            getNextCharacter();
            return true;
        }
        return false;
    }

    private boolean ifIdentifier(int currentLine, int currentColumn) throws Exception {
        if (Character.isLetter((char)this.character)) {
            StringBuilder identifier = new StringBuilder("" + (char)this.character);
            int numberOfLetters = 1;
            this.getNextCharacter();
            while (Character.isLetter((char)this.character) || Character.isDigit((char)this.character) || (char)this.character == '_') {
                identifier.append((char)this.character);
                if (++numberOfLetters >= MAX_IDENTIFIER_LENGTH)
                    throw new BadTokenSyntaxException("The length of identifier can't be so large", currentLine, currentColumn);
                this.getNextCharacter();
            }
            if (keyWords.containsKey(identifier.toString()))
                this.currentToken = new Token(keyWords.get(identifier.toString()),
                        identifier.toString(),
                        new TokenPosition(currentLine, currentColumn));
            else
                this.currentToken = new Token(TokenType.IDENTIFIER,
                        identifier.toString(),
                        new TokenPosition(currentLine, currentColumn));
            return true;
        }
        return false;
    }

    private boolean ifConstString(int currentLine, int currentColumn) throws Exception {
        if ((char) character == '"') {
            int numberOfConstSigns = 0;
            StringBuilder quotation = new StringBuilder();
            while (getNextCharacter() != -1) {
                if ((char) this.character != '"') {
                    quotation.append((char) this.character);
                    if (++numberOfConstSigns >= MAX_CONST_STRING_LENGTH)
                        throw new BadTokenSyntaxException("The length of quotation can't be so large", currentLine, currentColumn);
                }
                else {
                    this.currentToken = new Token(TokenType.CONST_STRING, quotation.toString(),
                            new TokenPosition(currentLine, currentColumn));
                    getNextCharacter();
                    return true;
                }
            }
            throw new BadTokenSyntaxException("Unclosed string literal", currentLine, currentColumn);
        }
        return false;
    }

    private void skipSpaces(int currentLine, int currentColumn) throws Exception {
        int numberOfSpaces = 0;
        while (Character.isWhitespace((char)this.character)) {
            if (++numberOfSpaces >= MAX_NUMBER_OF_SPACES)
                throw new BadTokenSyntaxException("The number of spaces can't be so large", currentLine, currentColumn);
            getNextCharacter();
        }
    }

    private void matchComparison(int currentLine, int currentColumn) throws IOException {
        switch ((char) this.character) {
            case '<' -> {
                getNextCharacter();
                if ((char) this.character == '=') {
                    this.currentToken = new Token(TokenType.LESS_OR_EQUALS, "<=",
                            new TokenPosition(currentLine, currentColumn));
                    getNextCharacter();
                } else {
                    this.currentToken = new Token(TokenType.LESS_THAN, "<",
                            new TokenPosition(currentLine, currentColumn));
                    return;
                }
            }
            case '>' -> {
                getNextCharacter();
                if ((char) this.character == '=') {
                    this.currentToken = new Token(TokenType.MORE_OR_EQUALS, ">=",
                            new TokenPosition(currentLine, currentColumn));
                    getNextCharacter();
                } else {
                    this.currentToken = new Token(TokenType.MORE_THAN, ">",
                            new TokenPosition(currentLine, currentColumn));
                    return;
                }
            }
            case '=' -> {
                getNextCharacter();
                if ((char) this.character == '=') {
                    this.currentToken = new Token(TokenType.EQUALS, "==",
                            new TokenPosition(currentLine, currentColumn));
                    getNextCharacter();
                } else {
                    this.currentToken = new Token(TokenType.ASSIGN, "=",
                            new TokenPosition(currentLine, currentColumn));
                    return;
                }
            }
            case '!' -> {
                getNextCharacter();
                if ((char) this.character == '=') {
                    this.currentToken = new Token(TokenType.NOT_EQUALS, "!=",
                            new TokenPosition(currentLine, currentColumn));
                    getNextCharacter();
                }else {
                    this.currentToken = new Token(TokenType.EXCLAMATION_MARK, "!",
                            new TokenPosition(currentLine, currentColumn));
                    return;
                }
            }
            default -> this.currentToken = new Token(TokenType.UNDEFINED, "",
                    new TokenPosition(currentLine, currentColumn));
        }

    }

    public void next() throws Exception {
        try {
            int currentLine = this.line;
            int currentColumn = this.column;

            this.skipSpaces(currentLine, currentColumn);

            currentLine = this.line;
            currentColumn = this.column;

            if (this.character==-1) {
                this.currentToken = new Token(TokenType.END_OF_FILE, "$");
                return;
            }

            if (this.ifIdentifier(currentLine, currentColumn)) return;
            if (this.ifNumber(currentLine, currentColumn)) return;
            if (this.ifLogicalOperators(currentLine, currentColumn)) return;
            if (this.ifConstString(currentLine, currentColumn)) return;
            if (this.ifComment(currentLine, currentColumn)) return;

            if (tokens.containsKey(Character.toString(this.character))) {
                this.currentToken = new Token(tokens.get(Character.toString(this.character)),
                        Character.toString(this.character), new TokenPosition(currentLine, currentColumn));
                getNextCharacter();
            }
            else
                this.matchComparison(currentLine, currentColumn);
        }
        catch (Exception e) { throw e; }
    }
}
