package scanner;

import java.util.Objects;

public class TokenPosition {
    private int line;
    private int column;

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public TokenPosition() {}

    public TokenPosition(int line, int column) {
        this.line = line;
        this.column = column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenPosition that = (TokenPosition) o;
        return getLine() == that.getLine() && getColumn() == that.getColumn();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLine(), getColumn());
    }
}
