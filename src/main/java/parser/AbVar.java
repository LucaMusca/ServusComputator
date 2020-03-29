package parser;

import java.util.Objects;

public class AbVar extends AbExp{
    Variable variable;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbVar)) return false;
        AbVar abVar = (AbVar) o;
        return Objects.equals(variable, abVar.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable);
    }

    public AbVar(Variable v) {
        this.variable = v;
    }


    double eval(double... in) {
        int i = Parser.variables.indexOf(variable);
        if(i!=-1) //variable is in variables
            if(i < in.length)
                return in[i];
            else
                throw new RuntimeException("No argument provided for variable " + stamp(this));
        throw new RuntimeException("cannot evaluate variable " + stamp(this));
    }

    @Override
    AbExp der(Variable v) {
        if(v.equals(this.variable))
            return AbNum.Num(1);
        return AbNum.Num(0);
    }

    @Override
    AbExp simplify() {
        return this;
    }

    @Override
    AbExp set(Variable v, double x) {
        if(variable.equals(v))
            return AbNum.Num(x);
        return this;
    }
    @Override
    AbExp set(Variable v, AbExp x) {
        if(variable.equals(v))
            return x;
        return this;
    }

    @Override
    public AbExp group() {
        return this;
    }

    @Override
    public String stamp(AbExp abExp) {
        return variable.toString();
    }

    @Override
    public AbExp copy() {
        return this;
    }
}
