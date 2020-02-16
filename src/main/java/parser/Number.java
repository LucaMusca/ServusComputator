package parser;

public class Number extends Token {
    public double value;

    Number(double value) {
        super(String.valueOf(value));
        this.value = value;
    }

    public double getValue() {
        return value;
    }



    @Override
    public String toString() {
        return string;
    }

}

