package parser;

public class AbCost extends AbExp{
    Constant c;

    public AbCost(Constant c) {
       this.c = c;
    }

    @Override
    double eval(double... in) {
        return c.getValue();
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
    public String stamp(AbExp abExp) {
        return c.getString();
    }
}
