package parser;

import java.util.Objects;

public class AbNum extends AbExp {

    private double value;

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbNum)) return false;
        AbNum abNum = (AbNum) o;
        return Double.compare(abNum.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    private AbNum(double value) {
        this.value = value;
    }

    public static AbNum Num(double value){
        return new AbNum(value);
    }

    @Override
    public String stamp(AbExp abExp) {
        if(value % 1 == 0)
            return String.valueOf((int)value);
        return String.valueOf(value);
    }

    @Override
    public AbExp copy() {
        return this;
    }

    @Override
    double eval(double... in) {
        return value;
    }

    AbExp der(Variable v) {
        return new AbNum(0);
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

    public static double getNum(AbExp a){
        AbNum n = (AbNum) a;
        return n.value;

    }
}
