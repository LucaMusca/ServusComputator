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

    public AbFun(Function function, AbExp arg) {
        this.arg = arg;
        this.function = function;
    }

    @Override
    public String stamp(AbExp abExp) {
        return function.toString() + "(" + arg.stamp(this) + ")";
    }

    @Override
    public AbExp copy() {
        return new AbFun(function, arg.copy());
    }

    @Override
    double eval(double... in) {
        return function.operation.fun(arg.eval(in));
    }

    AbExp der(Variable v) {
        return new AbOp(function.derivative.fun(arg), Parser.times,arg.der(v));
    } //chain rule

    @Override
    AbExp simplify() {
        arg = arg.simplify();
        if(arg instanceof AbNum)
            return AbNum.Num(function.operation.fun(arg.eval()));
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
