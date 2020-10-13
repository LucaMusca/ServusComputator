package parser;

import java.util.HashMap;

public class Utils {
    /*static AbExp summation(AbExp arg, Variable v, int start, int end) {
        AbExp out = AbNum.Num(0);
        for (int i = start; i <= end; i++) {
            AbExp tmp = arg.copy();
            out = new AbOp(out, Parser.plus, tmp.set(v, i));
        }
        return out;
    } */

    static AbNum newtonSolve(AbExp arg, Variable v, double seed, int iters) {
        AbNum out = AbNum.Num(seed);
        var a = arg.copy().set(v, out).simplify();
        if (!(a instanceof AbNum))
            throw new IllegalArgumentException(arg + " is not only a function of " + v);
        AbExp der = arg.copy().der(v);
        for (int i = 0; i < iters; i++) {
            a = arg.copy().set(v, out).simplify();
            out = out.subtract(
                    ((AbNum) a).divide((AbNum) der.copy().set(v, out).simplify())
            );
        }
        return out;
    }

    static AbNum newtonSolve(AbExp arg, Variable v, String s, int iters) {
        var seed = AbExp.toAbExp(s);
        if (!(seed instanceof AbNum))
            throw new IllegalArgumentException(seed + " is not a number");
        var out = (AbNum) seed;
        var a = arg.copy().set(v, out).simplify();
        if (!(a instanceof AbNum))
            throw new IllegalArgumentException(arg + " is not only a function of " + v);
        AbExp der = arg.copy().der(v);
        for (int i = 0; i < iters; i++) {
            a = arg.copy().set(v, out).simplify();
            out = out.subtract(
                    ((AbNum) a).divide((AbNum) der.copy().set(v, out).simplify())
            );
        }
        return out;
    }

    static AbExp taylorGroup(AbExp arg, Variable v, AbExp x0, int terms) {
        AddendsMap out = new AddendsMap();
        AbExp der = arg.copy();
        for (int n = 0; n < terms; n++) {
            Factors term = new Factors();
            term.put(new AbOp(new AbVar(v), Parser.minus, x0), n);
            var d = der.copy();
            d = d.set(v, x0);
            term.put(d);
            term.put(new RatNum(fact(n)), -1);
            out.put(term);
            der = der.der(v).simplify();
        }
        return out.simplify();
    }

    static AbExp taylorGroup(AbExp arg, Variable v, double x0, int terms) {
        AddendsMap out = new AddendsMap();
        AbExp der = arg.copy();
        AbExp base;
        if (x0 == 0)
            base = new AbVar(v);
        else
            base = new AbOp(new AbVar(v), Parser.minus, AbNum.Num(0));
        for (int n = 0; n < terms; n++) {
            Factors term = new Factors();
            term.put(base, n);
            var d = der.copy();
            d = d.set(v, x0);
            term.put(d);
            term.put(new RatNum(fact(n)), -1);
            out.put(term);
            der = der.der(v).simplify();
        }
        return out.simplify();
    }

    public static long fact(int n) {
        if (n <= 1)
            return 1L;

        if (factCache.containsKey(n))
            return factCache.get(n);

        long a = fact(n - 1) * ((long) n);
        factCache.put(n, a);
        return a;
    }

    static HashMap<Integer, Long> factCache = new HashMap<>();

    public static String fmt(double d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.format("%s", d);
    }

    public static StringBuilder wrapWithPar(StringBuilder s) {
        if (isWrapped(s))
            return s;
        else
            return new StringBuilder().append("(").append(s).append(")");

    }

    public static StringBuilder unWrap(StringBuilder s) {
        if (isWrapped(s)) {
            return s.deleteCharAt(s.length() - 1).deleteCharAt(0);
        }
        return s;
    }

    public static StringBuilder unWrap(String string) {
        return unWrap(new StringBuilder(string));
    }

    public static boolean isWrapped(String s) {
        return isWrapped(new StringBuilder(s));
    }

    public static boolean isWrapped(StringBuilder s) {
        if (s.length() < 2 || s.charAt(0) != '(')
            return false;
        int count = 1; // counts how many ( are open
        for (int i = 1; i < s.length() - 1; i++) {
            if (s.charAt(i) == '(')
                count++;
            if (s.charAt(i) == ')')
                count--;
            if (count == 0)
                return false;
        }
        return count == 1;
    }

    public static boolean isZero(double a) {
        return Math.abs(a) < 0.0001;
    }

    public static boolean isCloseTo(double in, double target) {
        return Math.abs(in - target) < 0.0001;
    }


}
