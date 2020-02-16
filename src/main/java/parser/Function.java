package parser;


public class Function extends Token {
    public Operation operation;
    public Derivative derivative;

    Function(String s, Operation o, Derivative d) {
        super(s);
        this.operation = o;
        this.derivative = d;
    }

    public double getValue() {
        throw new UnexpectedToken(this);
    }


    @Override
    public String toString() {
        return string;
    }

    interface Derivative {
       AbExp fun(AbExp t);
    }

    @FunctionalInterface
    interface Operation {
        double fun(double t1);
    }

}

class Sin extends Function {
    Sin() {
        super("sin", Math::sin, t -> new AbFun(new Cos(), t));
    }
}
class Cos extends Function {
    Cos() {
        super("cos", Math::cos, t -> new AbOp(AbNum.Num(0), Parser.minus, new AbFun(new Sin(), t)));
    }
}

class Exp extends Function {
    Exp() {
        super("exp", Math::exp, t -> new AbFun(new Exp(), t));
    }
}

class Ln extends Function {
    Ln() {
        super("ln", Math::log, t -> new AbOp(AbNum.Num(1), Parser.divide, t));
    }
}

class MinFun extends Function {
     MinFun() {
        super("-", a -> -a, t -> new AbFun(Parser.mn, t)); //TODO derivative is probably wrong
    }
}

class Sqrt extends Function{
    Sqrt() {
        super("sqrt", Math::sqrt, t -> new AbOp(AbNum.Num(1), Parser.divide, new AbOp(AbNum.Num(2),Parser.times,new AbFun(new Sqrt(),t))));
    }
}



