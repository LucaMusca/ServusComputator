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
    private static Sin instance;

    private Sin() {
        super("sin", Math::sin, t -> AbFun.create(Cos.getInstance(), t));
    }

    public static Sin getInstance() {
        if (instance == null) {
            instance = new Sin();
            return instance;
        }
        return instance;
    }
}

class Cos extends Function {
    static private Cos instance;

    private Cos() {
        super("cos", Math::cos, t -> new AbOp(AbNum.Num(0), Parser.minus, AbFun.create(Sin.getInstance(), t)));
    }

    public static Cos getInstance() {
        if (instance == null) {
            instance = new Cos();
            return instance;
        }
        return instance;
    }
}

class Tan extends Function {
    Tan() {
        super("tan", Math::tan, t -> new AbOp(AbFun.create(Cos.getInstance(), t), Parser.pow, AbNum.Num(-2)));
    }
}


class Exp extends Function {
    Exp() {
        super("exp", Math::exp, t -> AbFun.create(new Exp(), t));
    }
}

class Ln extends Function {
    Ln() {
        super("ln", Math::log, t -> new Factors(t, -1));
    }
}

class MinFun extends Function {
     MinFun() {
        super("-", a -> -a, t -> AbNum.Num(-1));
    }
}

class Sqrt extends Function{
    Sqrt() {
        super("sqrt", Math::sqrt, t -> new AbOp(AbNum.Num(1), Parser.divide,
                new AbOp(AbNum.Num(2), Parser.times, AbFun.create(new Sqrt(), t))));
    }
}

class DifferentialOperator extends Function {
    Variable[] variables;

    DifferentialOperator(String s, Variable... v) {
        super(s, t1 -> {
            throw new IllegalArgumentException();
        }, t2 -> {
            throw new IllegalArgumentException();
        });
        this.variables = v;
    }

}