package parser;


import java.util.Objects;

public abstract class AbNum extends AbExp {

    public static AbNum Num(double value) {
        if (Utils.isZero(value % 1))
            return new RatNum((int) value);
        return new IrrNum(value);
    }

    public abstract double getValue();

    public abstract AbNum add(AbNum a);

    public abstract AbNum subtract(AbNum a);

    public abstract AbNum multiply(AbNum a);

    public abstract AbNum divide(AbNum a);

    public abstract AbNum pow(double a);

    public abstract AbNum flipSign();

    public abstract AbNum reciprocal();


    @Override
    public AbExp copy() {
        return this;
    }

    AbExp der(Variable v) {
        return new RatNum(0);
    }

    @Override
    AbExp simplify() {
        return this;
    }

    @Override
    AbExp set(Variable v, double x) {
        return this;
    }

    @Override
    AbExp set(Variable v, AbExp x) {
        return this;
    }

    @Override
    public AbExp group() {
        return this;
    }

    @Override
    AbNum eval(double... in) {
        return this;
    }

    public abstract boolean isZero();

    public boolean isCloseTo(double target) {
        return Math.abs(getValue() - target) < 0.0001;
    }
}

class IrrNum extends AbNum {
    private double value;

    public IrrNum(double value) { //TODO
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public AbNum add(AbNum a) {
        return new IrrNum(this.getValue() + a.getValue());
    }

    public AbNum subtract(AbNum a) {
        return new IrrNum(this.getValue() - a.getValue());
    }

    public AbNum multiply(AbNum a) {
        return new IrrNum(this.getValue() * a.getValue());
    }

    public AbNum divide(AbNum a) {
        return new IrrNum(this.getValue() / a.getValue());
    }

    @Override
    public AbNum pow(double a) {
        return new IrrNum(Math.pow(getValue(), a));
    }

    @Override
    public AbNum flipSign() {
        return new IrrNum(-getValue());
    }

    @Override
    public AbNum reciprocal() {
        return new IrrNum(1 / getValue());
    }

    public AbExp simplify() {
        if (getValue() % 1 == 0)
            return new RatNum((int) value);
        return this;
    }

    @Override
    public boolean isZero() {
        return Math.abs(getValue()) < 0.0001;
    }

    @Override
    public boolean isCloseTo(double target) {
        return Math.abs(getValue() - target) < 0.0001;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IrrNum)) return false;
        IrrNum irrNum = (IrrNum) o;
        return Double.compare(irrNum.getValue(), getValue()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

    @Override
    public String stamp(AbExp abExp) {
        return Utils.fmt(value);
    }


}

class RatNum extends AbNum {
    private long numerator, denominator; // denominator is positive

    public RatNum(long numer, long denom) {
        if (denom == 0)
            throw new ArithmeticException("you're dividing by 0, check the input");

        // Make the numerator "store" the sign
        if (denom < 0) {
            numer = numer * -1;
            denom = denom * -1;
        }

        numerator = numer;
        denominator = denom;

        reduce();
    }

    public RatNum(long value) {
        this(value, 1L);
    }


    public RatNum reciprocal() {
        return new RatNum(denominator, numerator);
    }

    @Override
    public boolean isZero() {
        return numerator == 0;
    }

    private boolean overflows(RatNum n) {
        return Long.MAX_VALUE / denominator < n.denominator;
    }


    public AbNum add(AbNum op) {
        if (op instanceof RatNum) {
            var op2 = (RatNum) op;
            if (overflows(op2))
                return new IrrNum(op2.getValue() + getValue());
            long commonDenominator = denominator * op2.denominator;
            long numerator1 = numerator * op2.denominator;
            long numerator2 = op2.numerator * denominator;
            long sum = numerator1 + numerator2;
            var out = new RatNum(sum, commonDenominator);
            out.reduce();
            return out;
        } else {
            var op2 = (IrrNum) op;
            return new IrrNum(op2.getValue() + getValue());
        }
    }


    public AbNum subtract(AbNum op) {
        if (op instanceof RatNum) {
            var op2 = (RatNum) op;
            if (overflows(op2))
                return new IrrNum(getValue() - op2.getValue());
            long commonDenominator = denominator * op2.denominator;
            long numerator1 = numerator * op2.denominator;
            long numerator2 = op2.numerator * denominator;
            long difference = numerator1 - numerator2;
            var out = new RatNum(difference, commonDenominator);
            out.reduce();
            return out;
        } else {
            var op2 = (IrrNum) op;
            return new IrrNum(op2.getValue() - getValue());
        }
    }

    public AbNum multiply(AbNum op) {
        if (op instanceof RatNum) {
            var op2 = (RatNum) op;
            if (overflows(op2))
                return new IrrNum(op2.getValue() * getValue());
            long numer = numerator * op2.numerator;
            long denom = denominator * op2.denominator;
            return new RatNum(numer, denom);
        } else {
            var op2 = (IrrNum) op;
            return new IrrNum(op2.getValue() * getValue());
        }
    }

    public AbNum divide(AbNum op) {
        if (op instanceof RatNum) {
            var op2 = (RatNum) op;
            if (overflows(op2))
                return new IrrNum(getValue() / op2.getValue());
            return multiply(op2.reciprocal());
        } else {
            var op2 = (IrrNum) op;
            return new IrrNum(op2.getValue() / getValue());
        }
    }

    @Override
    public AbNum pow(double a) {
        if (Utils.isCloseTo(a, 1))
            return this;
        if (Utils.isCloseTo(a, -1))
            return this.reciprocal();

        if (a % 1 == 0) {
            var b = (int) a;
            if (b > 0)
                return new RatNum((int) Math.pow(numerator, b), (int) Math.pow(denominator, b));
            else
                return new RatNum((int) Math.pow(denominator, -b), (int) Math.pow(numerator, -b));
        }
        var out = new IrrNum(getValue());
        return out.pow(a);
    }

    @Override
    public AbNum flipSign() {
        return new RatNum(-numerator, denominator);
    }

    public String stamp(AbExp a) {
        if (numerator == 0)
            return "0";
        if (denominator == 1)
            return String.valueOf(numerator);


        return numerator + "/" + denominator;

    }

    public double getValue() {
        return ((double) numerator) / ((double) denominator);
    }

    private void reduce() {
        if (numerator != 0) {
            long common = gcd(Math.abs(numerator), denominator);

            numerator = numerator / common;
            denominator = denominator / common;
        }
    }

    private long gcd(long num1, long num2) {
        while (num1 != num2)
            if (num1 > num2)
                num1 = num1 - num2;
            else
                num2 = num2 - num1;

        return num1;
    }

    public boolean equals(Object op) {
        if (op instanceof RatNum) {
            final RatNum op2 = (RatNum) op;
            return (numerator == op2.numerator &&
                    denominator == op2.denominator);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator);
    }
}


