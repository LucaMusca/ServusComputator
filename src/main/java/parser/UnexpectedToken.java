package parser;

public class UnexpectedToken extends RuntimeException {
    public UnexpectedToken(Token t) {
        super("Unexpected token " + t.toString());
    }
}
