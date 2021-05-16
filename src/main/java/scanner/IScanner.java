package scanner;

public interface IScanner {
    Token getCurrentToken();
    void next() throws Exception;
}
