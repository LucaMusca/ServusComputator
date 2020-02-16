package parser;

public abstract class Token {

    String string;

    public Token(String s) {
        if (!(this instanceof Number)) {
            for (Character c : s.toCharArray()) {
                if (Character.isDigit(c) || c == '.') {
                    throw new IllegalStateException(String.format("Illegal token string %s", s));
                }
            }
        }
        string = s;
    }

    protected Token() {
    }

    String print() {
        return toString();
    }




    String getString() {
        return string;
    }

    public String toString() {
        return string;
    }


    Token unNest() {
        return this;
    }


}

abstract class Delimiter extends Token {


    Delimiter(String s) {
        super(s);
    }

    public String toString() {
        return string;
    }



}

class Left extends Delimiter {
    static Left leftDelimiter = new Left("Left Delimiter");

    Left(String s) {
        super(s);
    }


}

class Right extends Delimiter {
    static Right rightDelimiter = new Right("Right Delimiter");

    Right(String s) {
        super(s);
    }

}



