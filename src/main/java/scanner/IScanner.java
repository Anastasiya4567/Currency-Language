package scanner;

import scanner.token.Token;

public interface IScanner {
    Token getCurrentToken();
    void next() throws Exception;
}
