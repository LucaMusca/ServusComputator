package parser;

import java.util.ArrayList;
import java.util.List;

public class Addends extends AbExp{
    List<AbExp> pos;
    List<AbExp> neg;
    double known = 0;


    public Addends() {
        this.pos = new ArrayList<>();
        this.neg = new ArrayList<>();

    }

    public static Addends fromPlus(AbExp e1, AbExp e2){
        var a =  new Addends();
        a.addPos(e1);
        a.addPos(e2);

        return a;
    }

    public static Addends fromMinus(AbExp e1, AbExp e2){
        var a =  new Addends();
        a.addPos(e1);
        a.addNeg(e2);

        return a;
    }

    public void addPos(AbExp e1){

        if(e1.is(Times.class)){
            e1 =  Factors.fromTimes( ((AbOp) e1).left, ((AbOp) e1).right);
        }
        if(e1.is(Divide.class)){
            e1 = Factors.fromDivide( ((AbOp) e1).left, ((AbOp) e1).right);
        }

        if(e1.is(Plus.class)){

            addPos(((AbOp) e1).left);
            addPos(((AbOp) e1).right);
        }
        else if(e1.is(Minus.class)){
            addPos(((AbOp) e1).left);
            addNeg(((AbOp) e1).right);
        }
        else if(e1 instanceof Addends){
            mergeWith((Addends)e1);
        }
        else
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

        if(e1.is(Times.class)){
            e1 =  Factors.fromTimes( ((AbOp) e1).left, ((AbOp) e1).right);
        }
        if(e1.is(Divide.class)){
            e1 = Factors.fromDivide( ((AbOp) e1).left, ((AbOp) e1).right);
        }

        if(e1.is(Plus.class)){
            addNeg(((AbOp) e1).left);
            addNeg(((AbOp) e1).right);
        }
        else if(e1.is(Minus.class)){
            addNeg(((AbOp) e1).left);
            addPos(((AbOp) e1).right);
        }
        else
            neg.add(e1);
    }

    @Override
    double eval(double... in) {
        double n=0;
        for(var a : pos) {
            n+= a.eval(in);
        }
        double d=1;
        for(var a : neg) {
            d+= a.eval(in);
        }
        return known + n/d;
    }

    @Override
    AbExp set(Variable v, double x) {
        for (int i = 0; i < pos.size(); i++) {
            AbExp a = pos.get(i);
            pos.set(i,a.set(v, x));
        }
        for(int i = 0; i < neg.size(); i++) {
            AbExp a = neg.get(i);
            neg.set(i, a.set(v, x));
        }
        return this;

    }

    @Override
    AbExp set(Variable v, AbExp x) {
        for (int i = 0; i < pos.size(); i++) {
            AbExp a = pos.get(i);
            pos.set(i,a.set(v, x));
        }
        for(int i = 0; i < neg.size(); i++) {
            AbExp a = neg.get(i);
            neg.set(i, a.set(v, x));
        }
        return this;

    }

    @Override
    AbExp simplify() {

        for (int i = 0; i < pos.size(); i++) {
            pos.set(i,pos.get(i).simplify());
            if(pos.get(i).is(Number.class)){
                known+= ((AbNum) pos.get(i)).getValue();                          // put numbers in known
                pos.remove(i);
                i--;
            }
        }

        for (int i = 0; i < neg.size(); i++) {
            neg.set(i,neg.get(i).simplify());
            if(neg.get(i).is(Number.class)){
                known-= ((AbNum) neg.get(i)).getValue();                          // put numbers in known
                neg.remove(i);
                i--;
            }
        }

        pos: for (int i = 0; i < pos.size(); i++) {
            for (int j = 0; j < neg.size(); j++) {
                var p = pos.get(i);
                var n = neg.get(j);
                if(p.equals(n)){  // delete equal elements
                    pos.remove(i);
                    i--;
                    neg.remove(j);
                    continue pos;
                }
                  if(p instanceof Factors && n instanceof Factors) { // combine plus and minus elements
                    final Factors pf = (Factors) p;
                    final Factors nf = (Factors) n;
                    if(pf.hasShapeOf(nf)){
                        if(pf.coeff-nf.coeff > 0){
                            pf.coeff-=nf.coeff;
                            pos.set(i,pf);
                            i--; // to check again pos[i]
                            neg.remove(j);
                        }
                        else{
                            nf.coeff-=pf.coeff;
                            neg.set(j,nf);
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
            for (int j = i+1; j < pos.size(); j++) {
                var o = pos.get(i);
                var t = pos.get(j);
                if(o instanceof Factors && t instanceof Factors) {
                    final Factors of = (Factors) o;
                    final Factors tf = (Factors) t;
                    if(of.hasShapeOf(tf)){
                        of.coeff+=tf.coeff;
                        pos.set(i,of);
                        pos.remove(j);
                        j--;
                    }
                }
            }
        }

        // combine neg elements
        for (int i = 0; i < neg.size(); i++) {
            for (int j = i+1; j < neg.size(); j++) {
                var o = neg.get(i);
                var t = neg.get(j);
                if(o instanceof Factors && t instanceof Factors) {
                    final Factors of = (Factors) o;
                    final Factors tf = (Factors) t;
                    if(of.hasShapeOf(tf)){
                        of.coeff+=tf.coeff;
                        neg.set(i,of);
                        neg.remove(j);
                        j--;
                    }
                }
            }
        }

        if(pos.size() == 1)                     // unpack
            return new AbOp(AbNum.Num(known),Parser.plus,pos.get(0));
        if(neg.size() == 1)                     // unpack
            return new AbOp(AbNum.Num(known),Parser.minus,neg.get(0));

        if(pos.size() == 0 && neg.size() == 0)
            return AbNum.Num(known);
        return this;
    }


    private AbExp derSeq(List<AbExp> ls){
        return null;
    }

    //TODO
    // from here

    @Override
    AbExp der(Variable v) {
        known = 0;
        for (int i = 0; i < pos.size(); i++) {
            pos.set(i,pos.get(i).der(v));
        }

        for (int i = 0; i < neg.size(); i++) {
            neg.set(i,neg.get(i).der(v));
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
