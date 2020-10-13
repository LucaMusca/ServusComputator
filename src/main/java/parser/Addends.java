package parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


class AddendsMap extends AbExp {
    AbNum known = new RatNum(0);
    Map<AbExp, AbNum> map;

    AddendsMap(AbExp in) {
        if (in instanceof AddendsMap) {
            var a = ((AddendsMap) in);
            known = a.known;
            map = a.map;
            return;
        }
        if (in instanceof Factors) {
            var a = ((Factors) in);
            known = new RatNum(0);
            map = new HashMap<>();
            var c = a.coeff;
            a.coeff = new RatNum(1);
            put(a, c);
            return;
        }
        if (in instanceof AbNum) {
            known = (AbNum) in;
            map = new HashMap<>();
            return;
        }
        map = new HashMap<>();
        map.put(in, new RatNum(1));
    }

    AddendsMap() {
        map = new HashMap<>();
    }

    static AddendsMap merge(AddendsMap m1, AddendsMap m2) {

        var out = new AddendsMap();
        out.known = m1.known.add(m2.known);
        for (var s : m1.map.entrySet()) {
            out.put(s.getKey(), s.getValue());
        }
        for (var s : m2.map.entrySet()) {
            out.put(s.getKey(), s.getValue());
        }
        return out;
    }

    static AddendsMap invert(AddendsMap m) {
        m.known = m.known.flipSign();
        for (var s :
                m.map.entrySet()) {
            s.setValue(s.getValue().flipSign());
        }
        return m;
    }

    static AddendsMap multiply(AddendsMap m, AbNum coeff) {
        m.known = m.known.multiply(coeff);
        for (var s : m.map.entrySet()) {
            s.setValue(coeff.multiply(s.getValue()));
        }
        return m;
    }

    void put(AbExp b, AbNum c) {
        b = b.simplify();
        if (c.isZero())
            return;
        if (b instanceof AddendsMap) {
            var m = ((AddendsMap) b);
            var a = merge(this, multiply(m, c));
            known = a.known;
            map = a.map;
            return;
        }
        if (b instanceof AbNum) {
            var a = ((AbNum) b);
            a = a.multiply(c);
            known = known.add(a);
            return;
        }
        if (b instanceof Factors) {  // extract coefficient
            var m = ((Factors) b);
            c = c.multiply(m.coeff);
            m.coeff = new RatNum(1);
            if (m.map.size() == 0) {
                known = known.add(c);
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
                    map.put(b.group(), map.get(b).add(c));
                } else {
                    map.put(b.group(), c);
                }
                return;
            }
            b = m;
        }

        if (map.containsKey(b)) {
            map.put(b.group(), map.get(b).add(c));
        } else {
            map.put(b.group(), c);
        }
    }

    void put(AbExp b) {
        put(b, new RatNum(1));
    }

    @Override
    AbExp simplify() {
        var newMap = new HashMap<>(map);
        map = new HashMap<>();
        for (var s : newMap.entrySet()) { //TODO
            var a = s.getKey().simplify();
            put(a, s.getValue());
        }
        if (map.isEmpty())
            return known;
        if (map.size() == 1 && known.getValue() == 0) {
            for (var s : map.entrySet()) {
                if (Utils.isCloseTo(s.getValue().getValue(), 1))
                    return s.getKey();
                return new Factors(s.getValue(), s.getKey());
            }
        }
        return this;
    }


    @Override
    AbNum eval(double... in) {
        var out = known;
        for (var s : map.entrySet()) {
            out = out.add(s.getKey().eval(in).multiply((s.getValue())));
        }
        return out;
    }

    @Override
    AbExp der(Variable v) {
        var out = new AddendsMap();
        out.known = AbNum.Num(0);
        var newMap = new HashMap<>(map);
        out.map = new HashMap<>();
        for (var s : newMap.entrySet()) {
            out.put(s.getKey().der(v), s.getValue());
        }
        return out;
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
        var newMap = new HashMap<>(map);
        map = new HashMap<>();
        for (var s : newMap.entrySet()) {
            put(s.getKey().group(), s.getValue());
        }
        return this;
    }
    @Override
    public String stamp(AbExp abExp) {
        var pos = new StringBuilder();
        var neg = new StringBuilder();
        for (var s : map.entrySet()) {
            if (!s.getValue().isZero()) {  //TODO
                if (s.getValue().isCloseTo(1)) {
                    pos.append(" + ").append(s.getKey().stamp(this));
                    continue;
                }
                if (s.getValue().isCloseTo(-1)) {
                    neg.append(" - ").append(s.getKey().stamp(this));
                    continue;
                }
                if (s.getValue().getValue() > 0)
                    pos.append(" + ").append(s.getValue().stamp(this)).append(" ").append(s.getKey().stamp(this));
                else
                    neg.append(" - ").append(s.getValue().flipSign().stamp(this)).append(" ").append(s.getKey().stamp(this));

            }

        }
        var out = new StringBuilder();

        if (known.isZero()) {
            try {
                pos.delete(0, 3); //delete " * "
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            out.append(known.stamp(abExp));
        }

        if (pos.length() > 0) {
            out.append(pos);
        }

        if (neg.length() > 0) {
            try {
                neg.delete(0, 3); //delete " * "
            } catch (Exception e) {
                e.printStackTrace();
            }
            out.append(" - ").append(neg);
        }
        if (abExp instanceof Factors)
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
        return that.known.equals(known) &&
                Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(known, map);
    }
}