package parser;

import java.util.Objects;

public class AbFun extends AbExp {
    AbExp arg;
    Function function;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbFun)) return false;
        AbFun abFun = (AbFun) o;
        return arg.equals(abFun.arg) &&
                function.equals(abFun.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arg, function);
    }

    protected AbFun(Function function, AbExp arg) {
        this.arg = arg;
        this.function = function;
    }

    public static AbFun create(Function function, AbExp arg) {
        if (function instanceof DifferentialOperator)
            return new AbDiffOp((DifferentialOperator) function, arg);
        else
            return new AbFun(function, arg);
    }

    @Override
    public String stamp(AbExp abExp) {
        return function.toString() + "(" + arg.stamp(this) + ")";
    }

    @Override
    public AbExp copy() {
        return AbFun.create(function, arg.copy());
    }

    @Override
    AbNum eval(double... in) {
        return AbNum.Num(function.operation.fun(arg.eval(in).getValue()));
    }

    AbExp der(Variable v) {
        var f = new Factors();
        f.put(function.derivative.fun(arg.copy()));
        f.put(arg.copy().der(v));
        return f;
    } //chain rule

    @Override
    AbExp simplify() {
        arg = arg.simplify();
        if(arg instanceof AbNum)
            return AbNum.Num(function.operation.fun(arg.eval().getValue()));
        return this;
    }

     @Override
     AbExp set(Variable v, double x) {
         arg = arg.set(v,x);
         return this;
     }

     @Override
     AbExp set(Variable v, AbExp x) {
         arg = arg.set(v,x);
         return this;
     }

    @Override
    public AbExp group() {
        arg = arg.group();
        return this;
    }

}

class AbDiffOp extends AbFun {
    DifferentialOperator function;

    public AbDiffOp(DifferentialOperator function, AbExp arg) {
        super(function, arg);
        this.function = function;
    }

    @Override
    AbExp simplify() {
        return collapse().simplify();
    }

    @Override
    AbNum eval(double... in) {
        super.eval(in); // to be sure that the arg is evaluable
        return AbNum.Num(0);
    }

    @Override
    public AbExp group() {
        return collapse().simplify();
    }

    public AbExp collapse() {
        var a = arg.simplify().copy();
        for (Variable v : function.variables) {
            a = a.der(v);
        }
        return a;
    }
}