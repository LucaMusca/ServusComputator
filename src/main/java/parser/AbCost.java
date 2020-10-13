package parser;

public class AbCost extends AbExp{
    Constant c;

    public AbCost(Constant c) {
       this.c = c;
    }

    @Override
    AbNum eval(double... in) {
        return AbNum.Num(c.getValue());
    }

    @Override
    AbExp der(Variable v) {
        return AbNum.Num(0);
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
    public String stamp(AbExp abExp) {
        return c.getString();
    }

    @Override
    public AbExp copy() {
        return this;
    }
}
