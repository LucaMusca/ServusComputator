package parser;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Factors extends AbExp {
    AbNum coeff = new RatNum(1);
    Map<AbExp, Double> map;

    Factors(AbExp in) {
        if (in instanceof Factors) {
            var a = ((Factors) in);
            coeff = a.coeff;
            map = a.map;
            return;
        }
        map = new HashMap<>();
        map.put(base(in), exponent(in));
    }

    Factors(AbNum c, AbExp in) { // c* in : c is a coeff, not an exp
        if (in instanceof Factors) {
            var a = ((Factors) in);
            coeff = a.coeff.multiply(c);
            map = a.map;
            return;
        }
        map = new HashMap<>();
        coeff = coeff.multiply(c);
        map.put(base(in), exponent(in));
    }

    Factors(AbExp in, double exp) {  // in^exp
        map = new HashMap<>();
        put(in, exp);
    }

    Factors() {
        map = new HashMap<>();
    }


    static double exponent(AbExp in) {
        var ins = in.simplify();
        if (ins.is(Pow.class)) {
            var r = ((AbOp) ins).right;
            if (r instanceof AbNum) {
                return ((AbNum) r).getValue();
            }
        }
        return 1;
    }

    static AbExp base(AbExp in) {
        var ins = in.simplify();
        if (ins.is(Pow.class)) {
            var r = ((AbOp) ins).right.group();
            var s = ((AbOp) ins).left.group();
            if (r instanceof AbNum) {
                return s;
            }
        }
        return ins;
    }

    static Factors merge(Factors m1, Factors m2) {

        var out = new Factors();
        out.coeff = m1.coeff.multiply(m2.coeff);
        for (var s : m1.map.entrySet()) {
            out.put(s.getKey(), s.getValue());
        }
        for (var s : m2.map.entrySet()) {
            out.put(s.getKey(), s.getValue());
        }
        return out;
    }

    static Factors mergeInverse(Factors m1, Factors m2) {

        var out = new Factors();
        out.coeff = m1.coeff.divide(m2.coeff);
        for (var s : m1.map.entrySet()) {
            out.put(s.getKey(), s.getValue());
        }
        for (var s : m2.map.entrySet()) {
            out.put(s.getKey(), -s.getValue());
        }
        return out;
    }

    static Factors invert(Factors m) {
        m.coeff = m.coeff.reciprocal();
        for (var s :
                m.map.entrySet()) {
            s.setValue(-1 * s.getValue());
        }
        return m;
    }

    static Factors raise(Factors m, double exp) {
        m.coeff = m.coeff.pow(exp);
        for (var s :
                m.map.entrySet()) {
            s.setValue(exp * s.getValue());
        }
        return m;
    }

    void put(AbExp b, double e) {
        if (Utils.isZero(e))
            return;
        if (b instanceof Factors) {
            var m = ((Factors) b);
            var a = merge(this, raise(m, e));
            coeff = a.coeff;
            map = a.map;
            return;
        }
        if (b instanceof AbNum) {
            var a = ((AbNum) b);
            if (e == 1) {
                coeff = coeff.multiply(a);
                return;
            }
            if (e == -1) {
                coeff = coeff.divide(a);
                return;
            }
            a = a.pow(e);
            coeff = coeff.multiply(a);
            return;
        }
        var base = base(b);
        var exp = exponent(b) * e;
        if (map.containsKey(base)) {
            map.put(base, map.get(base) + exp);
        } else {
            map.put(base, exp);
        }
    }

    void put(AbExp b) {
        put(b, 1);
    }

    //TODO
    @Override
    AbNum eval(double... in) {
        var out = coeff;
        for (var s : map.entrySet()) {
            out = out.multiply(s.getKey().eval(in).pow(s.getValue()));
        }
        return out;
    }

    @Override
    AbExp der(Variable v) {
        AddendsMap addendsMap = new AddendsMap();
        for (var s : map.keySet()) {
            var tmp = this.copy();
            tmp.map.remove(s);
            double n = map.get(s);

            tmp.coeff = tmp.coeff.multiply(AbNum.Num(n));      // (f^n)' = n*f'*(f^(n-1)
            tmp.put(s, n - 1);
            var a = s.copy().der(v);
            tmp.put(a);

            addendsMap.put(tmp);
        }

        return addendsMap;
    }

    @Override
    AbExp simplify() {
        if (Utils.isZero(coeff.getValue()))
            return AbNum.Num(0);
        var newMap = new HashMap<>(map);
        map = new HashMap<>();
        for (var s : newMap.entrySet()) {
            var a = new AbOp(s.getKey(), Parser.pow, AbNum.Num(s.getValue())).simplify();
            put(a);
        }
        if (map.isEmpty())
            return coeff;
        return this;
    }

    @Override
    AbExp set(Variable v, double x) {
        var newMap = new HashMap<>(map);
        map = new HashMap<>();
        for (var s : newMap.entrySet()) {
            put(s.getKey().set(v, x), s.getValue());
        }
        return this;
    }

    @Override
    AbExp set(Variable v, AbExp x) {
        var newMap = new HashMap<>(map);
        map = new HashMap<>();
        for (var s : newMap.entrySet()) {
            put(s.getKey().set(v, x), s.getValue());
        }
        return this.simplify();
    }

    @Override
    public AbExp group() {
        var newMap = new HashMap<>(map);
        map = new HashMap<>();
        for (var s : newMap.entrySet()) {
            put(s.getKey().group(), s.getValue());
        }
        return this;
    }

    @Override
    public String stamp(AbExp abExp) {
        var num = new StringBuilder();
        int n = 0;
        int d = 0;
        var den = new StringBuilder();
        for (var s : map.entrySet()) {
            if (Utils.isZero(Math.abs(s.getValue()) - 1)) {  //TODO
                if (s.getValue() > 0) {
                    num.append(" * ").append(s.getKey().stamp(this));
                    n++;
                } else {
                    den.append(" * ").append(s.getKey().stamp(this));
                    d++;
                }
            } else {
                if (s.getValue() > 0) {
                    num.append(" * ").append(Utils.wrapWithPar(new StringBuilder(s.getKey().stamp(this)).append("^").append(Utils.fmt(s.getValue()))));
                    n++;
                } else {
                    den.append(" * ").append(Utils.wrapWithPar(new StringBuilder(s.getKey().stamp(this)).append("^").append(Utils.fmt(-s.getValue()))));
                    d++;
                }
            }
        }
        var out = new StringBuilder();
        if (n == 0 && d == 0)
            return coeff.stamp(abExp);
        try {
            num.delete(0, 3); //delete " * "
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Utils.isZero(Math.abs(coeff.getValue()) - 1)) {
            if (coeff.getValue() < 1)
                out.append("-");
        } else {
            out.append(coeff);
        }

        if (num.length() > 0) {
            out.append(num);
        }

        if (den.length() > 0) {
            try {
                den.delete(0, 3); //delete " * "
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (num.length() == 0)
                out = new StringBuilder(coeff.stamp(abExp));
            out.append(" / ");
            if (d > 1)
                out.append(Utils.wrapWithPar(den));
            else
                out.append(den);
        }

        return Utils.unWrap(out).toString();
    }

    @Override
    public Factors copy() {
        var out = new Factors();
        out.coeff = this.coeff;
        for (var s :
                this.map.entrySet()) {
            out.map.put(s.getKey().copy(), s.getValue());
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Factors)) return false;
        Factors factors = (Factors) o;
        return Objects.equals(coeff, factors.coeff) &&
                Objects.equals(map, factors.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coeff, map);
    }
}
