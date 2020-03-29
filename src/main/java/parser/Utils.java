package parser;

public class Utils {
   static AbExp summation(AbExp arg, Variable v, int start, int end){
        AbExp out = AbNum.Num(0);
        for (int i = start; i <= end; i++) {
            AbExp tmp = arg.copy();
            out = new AbOp(out,Parser.plus,tmp.set(v,i));
        }
        return out;
    }

    static AbExp taylor(AbExp arg, Variable v, AbExp x0, int terms){
        AbExp out = new Addends();
        AbExp der = arg.copy();
        for (int n = 0; n <= terms; n++) {
            AbExp term = new AbOp(new AbOp(new AbOp(new AbOp(new AbVar(v),Parser.minus,x0),Parser.pow,AbNum.Num(n)),Parser.times,der.copy().set(v,x0)),Parser.divide,AbNum.Num(fact(n))).simplify();
            out = new AbOp(out,Parser.plus,term);
            der = der.der(v).simplify();
        }
        return out;
    }

    static AbExp taylorGroup(AbExp arg, Variable v, AbExp x0, int terms){
        Addends out = new Addends();
        AbExp der = arg.copy();
        for (int n = 0; n <= terms; n++) {
            AbExp term = new AbOp(new AbOp(new AbOp(new AbOp(new AbVar(v),Parser.minus,x0),Parser.pow,AbNum.Num(n)),Parser.times,der.copy().set(v,x0)),Parser.divide,AbNum.Num(fact(n))).simplify();
            out.addPos(term);
            der = der.der(v).simplify();
        }
        return out;
    }

    static AbExp taylor(AbExp arg, Variable v, double x0, int terms){
        return taylor(arg, v, AbNum.Num(x0), terms);
    }

    static AbExp taylorGroup(AbExp arg, Variable v, double x0, int terms){
        return taylorGroup(arg, v, AbNum.Num(x0), terms);
    }



    public static int fact(int n) {
        int fact = 1;
        for (int i = 2; i <= n; i++) {
            fact = fact * i;
        }
        return fact;
    }

}

