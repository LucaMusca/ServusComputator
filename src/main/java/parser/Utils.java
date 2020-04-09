package parser;

public class Utils {
    static AbExp summation(AbExp arg, Variable v, int start, int end) {
        AbExp out = AbNum.Num(0);
        for (int i = start; i <= end; i++) {
            AbExp tmp = arg.copy();
            out = new AbOp(out, Parser.plus, tmp.set(v, i));
        }
        return out;
    }

    static AbExp taylor(AbExp arg, Variable v, AbExp x0, int terms) {
        AbExp out = new AddendsMap();
        AbExp der = arg.copy();
        for (int n = 0; n <= terms; n++) {
            AbExp term = new AbOp(new AbOp(new AbOp(new AbOp(new AbVar(v), Parser.minus, x0), Parser.pow, AbNum.Num(n)), Parser.times, der.copy().simplify().set(v, x0)), Parser.divide, AbNum.Num(fact(n))).simplify();
            out = new AbOp(out, Parser.plus, term);
            der = der.der(v).simplify();
        }
        return out;
    }

    static AbExp taylorGroup(AbExp arg, Variable v, AbExp x0, int terms) {
        AddendsMap out = new AddendsMap();
        AbExp der = arg.copy();
        for (int n = 0; n <= terms; n++) {
            FactorsMap term = new FactorsMap();
            term.put(new AbOp(new AbVar(v), Parser.minus, x0), n);
            term.put(der.copy().set(v, x0));
            term.put(AbNum.Num(fact(n)), -1);
            out.put(term);
            der = der.der(v).simplify();
        }
        return out.simplify();
    }

    static AbExp taylor(AbExp arg, Variable v, double x0, int terms) {
        return taylor(arg, v, AbNum.Num(x0), terms);
    }

    static AbExp taylorGroup(AbExp arg, Variable v, double x0, int terms) {
        return taylorGroup(arg, v, AbNum.Num(x0), terms);
    }

    public static int fact(int n) {
        int fact = 1;
        for (int i = 2; i <= n; i++) {
            fact = fact * i;
        }
        return fact;
    }

    public static String fmt(double d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.format("%s", d);
    }
    public static StringBuilder wrapWithPar(StringBuilder s){
        if(isWrapped(s) )
            return s;
        else
            return new StringBuilder().append("(").append(s).append(")");

    }
    public static StringBuilder unWrap(StringBuilder s){
        if(isWrapped(s) ){
           return s.deleteCharAt(0).deleteCharAt(s.length() -1);
        }
        return s;
    }
    public static boolean isWrapped(String s){
        return isWrapped(new StringBuilder(s));
    }
    public static boolean isWrapped(StringBuilder s){
        if(s.charAt(0) != '(')
            return false;
        int count = 1; // counts how many ( are open
        for (int i = 1; i < s.length() - 1; i++) {
            if(s.charAt(i) == '(')
                count++;
            if(s.charAt(i) == ')')
                count--;
            if(count == 0)
                return false;
        }
        return count == 1;
    }

    public static boolean isZero(double a){
        return Math.abs(a) < 0.0001;
    }
}

