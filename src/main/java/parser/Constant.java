package parser;

public class Constant extends Token {

    public double value;

    Constant(String string, double value) {
        super(string);

        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public Token eval(){
        return new Number(value);
    }

    @Override
    public String getString() {
        return string;
    }
}
