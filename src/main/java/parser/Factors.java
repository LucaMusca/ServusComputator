package parser;


import java.util.*;
import java.util.List;

public class Factors extends AbExp{
    List<AbExp> num;
    List<AbExp> den;
    double coeff = 1;

    private Factors() {
        this.num = new ArrayList<>();
        this.den = new ArrayList<>();

    }
    private Factors(List<AbExp> num) {
        this.num = num;
        this.den = new ArrayList<>();

    }

    public static Factors fromTimes(AbExp e1, AbExp e2){
        var f =  new Factors();
        f.addNum(e1);
        f.addNum(e2);

        return f;
    }

    public static Factors fromDivide(AbExp e1, AbExp e2){
        var f =  new Factors();
        f.addNum(e1);
        f.addDen(e2);

        return f;
    }

    private void addNum(AbExp e1){

        if(e1.is(Plus.class)){
            e1 =  Addends.fromPlus( ((AbOp) e1).left, ((AbOp) e1).right);
        }
        if(e1.is(Minus.class)){
            e1 = Addends.fromMinus(((AbOp) e1).left, ((AbOp) e1).right);
        }

        if(e1.is(Times.class)){
            addNum(((AbOp) e1).left);
            addNum(((AbOp) e1).right);
        }
        else if(e1.is(Divide.class)){
            addNum(((AbOp) e1).left);
            addDen(((AbOp) e1).right);
        }
        else if(e1.is(Pow.class)) {
            AbOp abOp = (AbOp) e1;
            var exp = abOp.right;
            exp = exp.simplify();
            if (!exp.is(Number.class))
                num.add(e1);
            else{
                var n = ((AbNum) exp).getValue();
                for (int i = 0; i < n; i++) {
                    addNum(((AbOp) e1).left);
                }
            }
        }
        else
            num.add(e1);
    }
    private void addDen(AbExp e1) {

        if(e1.is(Plus.class)){
            e1 =  Addends.fromPlus( ((AbOp) e1).left, ((AbOp) e1).right);
        }
        if(e1.is(Minus.class)){
            e1 = Addends.fromMinus(((AbOp) e1).left, ((AbOp) e1).right);
        }

        if(e1.is(Times.class)){
            addDen(((AbOp) e1).left);
            addDen(((AbOp) e1).right);
        }
        else if(e1.is(Divide.class)){
            addDen(((AbOp) e1).left);
            addNum(((AbOp) e1).right);
        }
        else if(e1.is(Pow.class)) {
            AbOp abOp = (AbOp) e1;
            var exp = abOp.right;
            exp = exp.simplify();
            if (!exp.is(Number.class))
                den.add(e1);
            else{
                var n = ((AbNum) exp).getValue();
                for (int i = 0; i < n; i++) {
                    addDen(((AbOp) e1).left);
                }
            }
        }
        else
            den.add(e1);
    }

    private boolean equalLists(List<AbExp> one, List<AbExp> two){
        return one.size() == two.size() && one.containsAll(two) && two.containsAll(one);

    }

    // remember to simplify before calling
    public boolean hasShapeOf(Factors o){
        return equalLists(num, o.num) && equalLists(den, o.den);
    }
    @Override
    double eval(double... in) {
        double n=1;
        for(var a : num) {
            n*= a.eval(in);
        }
        double d=1;
        for(var a : den) {
            d*= a.eval(in);
        }
        return coeff * n/d;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Factors)) return false;
        Factors factors = (Factors) o;
        return Double.compare(factors.coeff, coeff) == 0 &&
                hasShapeOf(((Factors) o));
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, den, coeff);
    }

    @Override
    AbExp set(Variable v, double x) {
        List<String> ls = new ArrayList<>(List.of("asd", "vc"));
        ls.sort(Comparator.comparing(String::length));
        for (int i = 0; i < num.size(); i++) {
            AbExp a = num.get(i);
            num.set(i,a.set(v, x));
        }
        for(int i = 0; i < den.size(); i++) {
            AbExp a = den.get(i);
            den.set(i, a.set(v, x));
        }
        return this;

    }

    @Override
    AbExp simplify() {

        for (int i = 0; i < num.size(); i++) {
            num.set(i,num.get(i).simplify());
            if(num.get(i).is(Number.class)){
                coeff*= ((AbNum) num.get(i)).getValue();                          // put numbers in coeff
                num.remove(i);
                i--;
            }
        }

        for (int i = 0; i < den.size(); i++) {
            den.set(i,den.get(i).simplify());
            if(den.get(i).is(Number.class)){
                coeff/= ((AbNum) den.get(i)).getValue();                          // put numbers in coeff
                den.remove(i);
                i--;
            }
        }

        num: for (int i = 0; i < num.size(); i++) {
            for (int j = 0; j < den.size(); j++) {
                if(num.get(i).equals(den.get(j))){
                    num.remove(i);
                    i--;
                    den.remove(j);
                    continue num;
                }
            }
        }

        //TODO: it's to simplify pow instances without having to expand them
         /*  {


            num:
            for (int i = 0; i < num.size(); i++) {
                for (int j = 0; j < den.size(); j++) {
                    var p = num.get(i);
                    var n = den.get(j);
                    if (p.equals(n)) {  // delete equal elements
                        num.remove(i);
                        i--;
                        den.remove(j);
                        continue num;
                    }
                    if (p.is(Pow.class) && n.is(Pow.class)) { // combine plus and minus elements
                        final AbOp pf = (AbOp) p;
                        final AbOp nf = (AbOp) n;
                        if (pf.left.equals(nf.left) && pf.right.is(Number.class) && nf.right.is(Number.class)) {
                            if (pf.ri - nf.coeff > 0) {
                                pf.coeff -= nf.coeff;
                                num.set(i, pf);
                                i--; // to check again num[i]
                                den.remove(j);
                            } else {
                                nf.coeff -= pf.coeff;
                                den.set(j, nf);
                                num.remove(i);
                                i--;
                            }
                            continue num;
                        }
                    }
                }
            }

            // combine plus elements
            for (int i = 0; i < num.size(); i++) {
                for (int j = i + 1; j < num.size(); j++) {
                    var o = num.get(i);
                    var t = num.get(j);
                    if (o instanceof Factors && t instanceof Factors) {
                        final Factors of = (Factors) o;
                        final Factors tf = (Factors) t;
                        if (of.hasShapeOf(tf)) {
                            of.coeff += tf.coeff;
                            num.set(i, of);
                            num.remove(j);
                            j--;
                        }
                    }
                }
            }

            // combine den elements
            for (int i = 0; i < den.size(); i++) {
                for (int j = i + 1; j < den.size(); j++) {
                    var o = den.get(i);
                    var t = den.get(j);
                    if (o instanceof Factors && t instanceof Factors) {
                        final Factors of = (Factors) o;
                        final Factors tf = (Factors) t;
                        if (of.hasShapeOf(tf)) {
                            of.coeff += tf.coeff;
                            den.set(i, of);
                            den.remove(j);
                            j--;
                        }
                    }
                }
            }


        }

*/



        if(num.size() == 1)                     // unpack
            return new AbOp(AbNum.Num(coeff),Parser.times,num.get(0));
        if(den.size() == 1)                     // unpack
            return new AbOp(AbNum.Num(coeff),Parser.divide,den.get(0));
        if(num.size() == 0 && den.size() == 0)
            return AbNum.Num(coeff);           // doesn't change the object internally
        return this;
    }

    @Override
    AbExp set(Variable v, AbExp x) {
        for (int i = 0; i < num.size(); i++) {
            AbExp a = num.get(i);
            num.set(i,a.set(v, x));
        }
        for(int i = 0; i < den.size(); i++) {
            AbExp a = den.get(i);
            den.set(i, a.set(v, x));
        }
        return this;

    }

    private AbExp derSeq(List<AbExp> ls, Variable v){
        var addends = new Addends();
        for (var l: ls) {
            var n = new ArrayList<>(ls);
            n.remove(l);
            n.add(l.der(v));
            addends.addPos(new Factors(n));
        }
        return addends;
    }

    //TODO
    // from here

    @Override
    AbExp der(Variable v) {
        var f = num;
        var g = den;
        var fp = derSeq(f,v);
        var gp = derSeq(g,v);

        Factors factors = new Factors();
        factors.addNum(new AbOp(new AbOp(fp,Parser.times,new Factors(g)),Parser.minus,new AbOp(new Factors(f),Parser.times,gp)));
        factors.addDen(new AbOp(new Factors(g),Parser.pow,AbNum.Num(2)));

        return factors;
    }

    @Override
    public AbExp group() { //TODO
        return this;
    }

    @Override
    public String stamp(AbExp abExp) {
        return String.format("%s * %s / %s)", coeff, num, den);
    }

    @Override
    public AbExp copy() {
        var out = new Factors();
        List<AbExp> n = new ArrayList<>();
        List<AbExp> d = new ArrayList<>();
        for (AbExp exp : num) {
            n.add(exp.copy());
        }
        for (AbExp abExp : den) {
            d.add(abExp.copy());
        }
        out.den = d;
        out.num = n;
        out.coeff = coeff;

        return out;
    }

}
