package parser;


import java.util.ArrayList;
import java.util.List;


public abstract class Operator extends Token {
    protected List<Simplifier> simplifiers = new ArrayList<>(); //list so it's possible to loop through each one and eventually go back

    public Operator(String s) {
        super(s);
    }
    Operator(String s, Operation o,Derivative derivative) {
        super(s);
        this.operation = o;
        this.derivative = derivative;
    }

    @FunctionalInterface
    public interface Derivative{
        AbExp fun(AbExp e1, AbExp e2, Variable x);
    }

    interface Operation {
        double fun(double t1, double t2);

    }

    public double operate(double t1, double t2){
        return operation.fun(t1, t2);
    }

    interface Simplifier{
        Tuple simplify(AbExp e1, AbExp e2);
    }

    public AbExp simplify(AbExp e1, AbExp e2){
        AbExp out = new AbOp(e1,this,e2);
        for (var s: simplifiers) {
            var tuple = s.simplify(e1, e2);
            if(tuple.bool)
                out = tuple.exp;

            if(out instanceof AbOp && getClass().isInstance(((AbOp) out).operator)) {
                var o = ((AbOp) out);
                e1 = o.left;
                e2 = o.right;
            }
            else
                break;
        }
        return out;
    }
    public Operation operation;
    public Derivative derivative;

    public String toString() {
        return string;
    }

}

 class Plus extends Operator {
    Plus() {
        super("+");
        this.operation = Double::sum;
        this.derivative = (e1,e2,x) -> new AbOp(e1.der(x),this,e2.der(x));

        simplifiers.addAll(List.of(
                (e1, e2) -> new Tuple(e2.is(0) , e1),
                (e1, e2) -> new Tuple(e1.is(0) && string.equals("-"),new AbFun(Parser.mn,e2)),
                (e1, e2) -> new Tuple(e1.is(0) && !string.equals("-"), e2)
        ));
    }

}

 class Minus extends Operator {
    Minus() {
        super("-");
        this.operation = (a,b) -> a - b;
        this.derivative = (e1,e2,x) -> new AbOp(e1.der(x),this,e2.der(x));
        simplifiers.addAll(List.of(
                (e1, e2) -> new Tuple(e2.is(0) , e1),
                (e1, e2) -> new Tuple(e1.is(0),new AbFun(Parser.mn,e2)),
                (e1, e2) -> new Tuple(e1.equals(e2), AbNum.Num(0))
        ));
    }
}


class Times extends Operator{
    Times() {
        super("*", (a, b) -> a * b,
                //product rule
                (e1,e2,x) ->
                        new AbOp(new AbOp(e1.der(x),Parser.times,e2),
                                Parser.plus,
                                new AbOp(e1,Parser.times,e2.der(x))));
        simplifiers.addAll(List.of(
                (e1, e2) -> new Tuple(e2.is(0), AbNum.Num(0)),
                (e1, e2) -> new Tuple(e1.is(0) , AbNum.Num(0)),
                (e1, e2) -> new Tuple(e1.is(1), e2),
                (e1, e2) -> new Tuple(e2.is(1), e1),
                (e1,e2) -> new Tuple(e1.equals(e2), new AbOp(e1,Parser.pow, AbNum.Num(2))),
                (e1, e2) -> new Tuple(e2 instanceof AbNum, new AbOp(e2,Parser.times,e1))
        ));
    }

}

class Divide extends Operator{
    Divide() {
        super("/", (a, b) -> a / b,
                (e1,e2,x) -> new AbOp(
                        new AbOp(new AbOp(e1.der(x),Parser.times,e2),
                        Parser.minus,
                        new AbOp(e1,Parser.times,e2.der(x))),
                        Parser.divide,
                        new AbOp(e2,Parser.times,e2)) // e2^2
        );
        simplifiers.addAll(List.of(
                (e1, e2) -> new Tuple(e1.is(0) , AbNum.Num(0)),
                (e1, e2) -> new Tuple(e2.is(1) , e1),
                (e1, e2) -> new Tuple(e1.equals(e2),AbNum.Num(1))
        ));
    }

}

class Pow extends Operator{
    Pow(){
        super("^",Math::pow,
                (f,g,x) -> {
                    if(g instanceof AbNum)                  // special case to speed things up
                        return new AbOp(g,Parser.times,new AbOp(new AbOp(f,Parser.pow, AbNum.Num(((AbNum) g).getValue() - 1)),Parser.times,f.der(x)));
                    return new AbOp(new AbOp(f, Parser.pow, g),
                            Parser.times,
                            new AbOp(new AbOp(g.der(x), Parser.times, new AbFun(Parser.ln, f)),
                                    Parser.plus,
                                    new AbOp(new AbOp(f.der(x), Parser.times, g),
                                            Parser.divide,
                                            f)));
                });

        simplifiers.addAll(List.of(
                (e1, e2) -> new Tuple(e1.is(0) && !e2.is(0), AbNum.Num(0)),
                (e1, e2) -> new Tuple(e2.is(0) , AbNum.Num(1)),
                (e1, e2) -> new Tuple(e1.is(1), AbNum.Num(1)),
                (e1, e2) -> new Tuple(e2.is(1), e1)
        ));
    }
}


// this class is the output of a Simplifier
class Tuple{
    boolean bool;
    AbExp exp;

    public Tuple(boolean bool, AbExp exp) {
        this.bool = bool;
        this.exp = exp;
    }
}

