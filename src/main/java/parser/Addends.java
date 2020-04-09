package parser;

import java.util.*;

public class Addends extends AbExp {
    List<AbExp> pos;
    List<AbExp> neg;
    double known = 0;


    public Addends() {
        this.pos = new ArrayList<>();
        this.neg = new ArrayList<>();

    }

    public static Addends fromPlus(AbExp e1, AbExp e2) {
        var a = new Addends();
        a.addPos(e1);
        a.addPos(e2);

        return a;
    }

    public static Addends fromMinus(AbExp e1, AbExp e2) {
        var a = new Addends();
        a.addPos(e1);
        a.addNeg(e2);

        return a;
    }

    public void addPos(AbExp e1) {

        if (e1.is(Times.class)) {
            e1 = Factors.fromTimes(((AbOp) e1).left, ((AbOp) e1).right);
        }
        if (e1.is(Divide.class)) {
            e1 = Factors.fromDivide(((AbOp) e1).left, ((AbOp) e1).right);
        }

        if (e1.is(Plus.class)) {

            addPos(((AbOp) e1).left);
            addPos(((AbOp) e1).right);
        } else if (e1.is(Minus.class)) {
            addPos(((AbOp) e1).left);
            addNeg(((AbOp) e1).right);
        } else if (e1 instanceof Addends) {
            mergeWith((Addends) e1);
        } else
            pos.add(e1);
    }

    private void mergeWith(Addends e1) {
        for (int i = 0; i < e1.pos.size(); i++) {
            addPos(e1.pos.get(i));
        }
        for (int i = 0; i < e1.neg.size(); i++) {
            addNeg(e1.neg.get(i));
        }
    }

    public void addNeg(AbExp e1) {

        if (e1.is(Times.class)) {
            e1 = Factors.fromTimes(((AbOp) e1).left, ((AbOp) e1).right);
        }
        if (e1.is(Divide.class)) {
            e1 = Factors.fromDivide(((AbOp) e1).left, ((AbOp) e1).right);
        }

        if (e1.is(Plus.class)) {
            addNeg(((AbOp) e1).left);
            addNeg(((AbOp) e1).right);
        } else if (e1.is(Minus.class)) {
            addNeg(((AbOp) e1).left);
            addPos(((AbOp) e1).right);
        } else
            neg.add(e1);
    }

    @Override
    double eval(double... in) {
        double n = 0;
        for (var a : pos) {
            n += a.eval(in);
        }
        double d = 1;
        for (var a : neg) {
            d += a.eval(in);
        }
        return known + n / d;
    }

    @Override
    AbExp set(Variable v, double x) {
        for (int i = 0; i < pos.size(); i++) {
            AbExp a = pos.get(i);
            pos.set(i, a.set(v, x));
        }
        for (int i = 0; i < neg.size(); i++) {
            AbExp a = neg.get(i);
            neg.set(i, a.set(v, x));
        }
        return this;

    }

    @Override
    AbExp set(Variable v, AbExp x) {
        for (int i = 0; i < pos.size(); i++) {
            AbExp a = pos.get(i);
            pos.set(i, a.set(v, x));
        }
        for (int i = 0; i < neg.size(); i++) {
            AbExp a = neg.get(i);
            neg.set(i, a.set(v, x));
        }
        return this;

    }

    @Override
    AbExp simplify() {

        for (int i = 0; i < pos.size(); i++) {
            pos.set(i, pos.get(i).simplify());
            if (pos.get(i).is(Number.class)) {
                known += ((AbNum) pos.get(i)).getValue();                          // put numbers in known
                pos.remove(i);
                i--;
            }
        }

        for (int i = 0; i < neg.size(); i++) {
            neg.set(i, neg.get(i).simplify());
            if (neg.get(i).is(Number.class)) {
                known -= ((AbNum) neg.get(i)).getValue();                          // put numbers in known
                neg.remove(i);
                i--;
            }
        }

        pos:
        for (int i = 0; i < pos.size(); i++) {
            for (int j = 0; j < neg.size(); j++) {
                var p = pos.get(i);
                var n = neg.get(j);
                if (p.equals(n)) {  // delete equal elements
                    pos.remove(i);
                    i--;
                    neg.remove(j);
                    continue pos;
                }
                if (p instanceof Factors && n instanceof Factors) { // combine plus and minus elements
                    final Factors pf = (Factors) p;
                    final Factors nf = (Factors) n;
                    if (pf.hasShapeOf(nf)) {
                        if (pf.coeff - nf.coeff > 0) {
                            pf.coeff -= nf.coeff;
                            pos.set(i, pf);
                            i--; // to check again pos[i]
                            neg.remove(j);
                        } else {
                            nf.coeff -= pf.coeff;
                            neg.set(j, nf);
                            pos.remove(i);
                            i--;
                        }
                        continue pos;
                    }
                }
            }
        }

        // combine plus elements
        for (int i = 0; i < pos.size(); i++) {
            for (int j = i + 1; j < pos.size(); j++) {
                var o = pos.get(i);
                var t = pos.get(j);
                if (o instanceof Factors && t instanceof Factors) {
                    final Factors of = (Factors) o;
                    final Factors tf = (Factors) t;
                    if (of.hasShapeOf(tf)) {
                        of.coeff += tf.coeff;
                        pos.set(i, of);
                        pos.remove(j);
                        j--;
                    }
                }
            }
        }

        // combine neg elements
        for (int i = 0; i < neg.size(); i++) {
            for (int j = i + 1; j < neg.size(); j++) {
                var o = neg.get(i);
                var t = neg.get(j);
                if (o instanceof Factors && t instanceof Factors) {
                    final Factors of = (Factors) o;
                    final Factors tf = (Factors) t;
                    if (of.hasShapeOf(tf)) {
                        of.coeff += tf.coeff;
                        neg.set(i, of);
                        neg.remove(j);
                        j--;
                    }
                }
            }
        }

        if (pos.size() == 1)                     // unpack
            return new AbOp(AbNum.Num(known), Parser.plus, pos.get(0));
        if (neg.size() == 1)                     // unpack
            return new AbOp(AbNum.Num(known), Parser.minus, neg.get(0));

        if (pos.size() == 0 && neg.size() == 0)
            return AbNum.Num(known);
        return this;
    }


    private AbExp derSeq(List<AbExp> ls) {
        return null;
    }

    //TODO
    // from here

    @Override
    AbExp der(Variable v) {
        known = 0;
        for (int i = 0; i < pos.size(); i++) {
            pos.set(i, pos.get(i).der(v));
        }

        for (int i = 0; i < neg.size(); i++) {
            neg.set(i, neg.get(i).der(v));
        }
        return this;
    }

    @Override
    public AbExp group() {
        return this;
    }

    @Override
    public String stamp(AbExp abExp) {
        return String.format("(%s + %s - %s)", known, pos, neg);
    }


    @Override
    public AbExp copy() {
        var out = new Addends();
        List<AbExp> p = new ArrayList<>();
        List<AbExp> n = new ArrayList<>();
        for (AbExp exp : pos) {
            p.add(exp.copy());
        }
        for (AbExp abExp : neg) {
            n.add(abExp.copy());
        }
        out.pos = p;
        out.neg = n;
        out.known = known;

        return out;
    }

}

class AddendsMap extends AbExp {
    double known = 0;
    Map<AbExp, Double> map;

    AddendsMap(AbExp in) {
        if (in instanceof AddendsMap) {
            var a = ((AddendsMap) in);
            known = a.known;
            map = a.map;
            return;
        }
        if (in instanceof FactorsMap) {
            var a = ((FactorsMap) in);
            known = 0;
            map = new HashMap<>();
            var c = a.coeff;
            a.coeff = 1;
            put(a, c);
            return;
        }
        if (in.is(Number.class)) {
            known = ((AbNum) in).getValue();
            map = new HashMap<>();
            return;
        }
        map = new HashMap<>();
        map.put(in, 1.);
    }

    AddendsMap() {
        map = new HashMap<>();
    }

    static double coeff(AbExp in) {
        var ins = in.simplify();
        if (ins.is(Times.class)) {
            var r = ((AbOp) ins).right;
            var l = ((AbOp) ins).left;
            if (r.is(Number.class)) {
                return ((AbNum) r).getValue();
            }
            if (l.is(Number.class)) {
                return ((AbNum) l).getValue();
            }
        }
        return 0;
    }

    static AbExp factor(AbExp in) {
        var ins = in.simplify();
        if (ins.is(Pow.class)) {
            var r = ((AbOp) ins).right;
            var s = ((AbOp) ins).left;
            if (r.is(Number.class)) {
                return s;
            }
            if (s.is(Number.class)) {
                return r;
            }
        }
        return ins;
    }

    static AddendsMap merge(AddendsMap m1, AddendsMap m2) {

        var out = new AddendsMap();
        out.known = m1.known + m2.known;
        for (var s : m1.map.entrySet()) {
            out.put(s.getKey(), s.getValue());
        }
        for (var s : m2.map.entrySet()) {
            out.put(s.getKey(), s.getValue());
        }
        return out;
    }

    static AddendsMap invert(AddendsMap m) {
        m.known = -m.known;
        for (var s :
                m.map.entrySet()) {
            s.setValue(-1 * s.getValue());
        }
        return m;
    }

    static AddendsMap multiply(AddendsMap m, double coeff) {
        m.known *= coeff;
        for (var s : m.map.entrySet()) {
            s.setValue(coeff * s.getValue());
        }
        return m;
    }

    void put(AbExp b, double c) {
        if (b instanceof AddendsMap) {
            var m = ((AddendsMap) b);
            var a = merge(this, multiply(m, c));
            known = a.known;
            map = a.map;
            return;
        }
        if (b.is(Number.class)) {
            var a = ((AbNum) b).getValue();
            a *= c;
            known += a;
            return;
        }
        if (b instanceof FactorsMap) {  // extract coefficient
            var m = ((FactorsMap) b);
            c *= m.coeff;
            m.coeff = 1;
            if (m.map.size() == 0) {
                known += c;
                return;
            }
            if (m.map.size() == 1) {
                for (var s : m.map.entrySet()) {
                    if (s.getValue() == 1) {
                        map.put(s.getKey(), c);
                        return;
                    }
                }

                if (map.containsKey(b)) {
                    map.put(b.group(), map.get(b) + c);
                } else {
                    map.put(b.group(), c);
                }
                return;
            }
            b = m;
        }

        if (map.containsKey(b)) {
            map.put(b.group(), map.get(b) + c);
        } else {
            map.put(b.group(), c);
        }
    }

    void put(AbExp b) {
        put(b, 1);
    }

    @Override
    AbExp simplify() {
        var newMap = new HashMap<>(map);
        map = new HashMap<>();
        for (var s : newMap.entrySet()) { //TODO
            var a = s.getKey().simplify();
            put(a,s.getValue());
        }
        if (map.isEmpty())
            return AbNum.Num(known);
        return this;
    }

    //TODO

    @Override
    double eval(double... in) {
        var out = known;
        for (var s : map.entrySet()) {
            out += s.getKey().eval(in) * s.getValue();
        }
        return out;
    }

    @Override
    AbExp der(Variable v)  {
        var newMap = new HashMap<>(map);
        map = new HashMap<>();
        for (var s : newMap.entrySet()) {
            put(s.getKey().der(v), s.getValue());
        }
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
        return this;
    }


    // end of TO DO
    @Override
    public AbExp group() {
        return this;
    }

    @Override
    public String stamp(AbExp abExp) {
        var pos = new StringBuilder();
        var neg = new StringBuilder();
        for (var s : map.entrySet()) {
            if (Utils.isZero(s.getValue())) {  //TODO
                if (Utils.isZero(Math.abs(s.getValue()) - 1)) {
                    if (s.getValue() > 0)
                        pos.append(" + ").append(s.getKey().stamp(this));
                    else
                        neg.append(" - ").append(s.getKey().stamp(this));
                    continue;
                }
                if (s.getValue() > 0)
                    pos.append(" + ").append(Utils.fmt(s.getValue())).append("*").append(s.getKey().stamp(this));
                else
                    neg.append(" - ").append(Utils.fmt(-s.getValue())).append("*").append(s.getKey().stamp(this));

            }

        }
        var out = new StringBuilder();

        if (Utils.isZero(known)) {
            try {
                pos.delete(0, 3); //delete " * "
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            out.append(Utils.fmt(known));
        }

        if (pos.length() > 0) {
            out.append(pos);
        }

        if (neg.length() > 0) {
            neg.delete(0, 3);
            out.append(" - ").append(neg);
        }
        if (abExp instanceof FactorsMap)
            return Utils.wrapWithPar(out).toString();
        else
            return Utils.unWrap(out).toString();

    }

    @Override
    public AbExp copy() {
        var out = new AddendsMap();
        out.known = this.known;
        for (var s :
                this.map.entrySet()) {
            out.map.put(s.getKey().copy(), s.getValue());
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddendsMap)) return false;
        AddendsMap that = (AddendsMap) o;
        return Double.compare(that.known, known) == 0 &&
                Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(known, map);
    }
}