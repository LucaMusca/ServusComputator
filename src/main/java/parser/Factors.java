package parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Factors extends AbExp{
    List<AbExp> num;
    List<AbExp> den;

    private Factors() {
        this.num = new ArrayList<>();
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
        if(e1.is(Times.class)){
            addNum(((AbOp) e1).left);
            addNum(((AbOp) e1).right);
        }
        else if(e1.is(Divide.class)){
            addNum(((AbOp) e1).left);
            addDen(((AbOp) e1).right);
        }
        else
            num.add(e1);
    }
    private void addDen(AbExp e1) {
        if(e1.is(Times.class)){
            addDen(((AbOp) e1).left);
            addDen(((AbOp) e1).right);
        }
        else if(e1.is(Divide.class)){
            addDen(((AbOp) e1).left);
            addNum(((AbOp) e1).right);
        }
        else
            den.add(e1);
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
        return n/d;
    }

    @Override
    AbExp set(Variable v, double x) {
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

    //TODO

    @Override
    AbExp simplify() {
        for (Iterator<AbExp> numIt = num.iterator(); numIt.hasNext();) {
                AbExp n = numIt.next();
            for (Iterator<AbExp> denIt = den.iterator(); denIt.hasNext(); ) {
                AbExp d = denIt.next();
                if (n.equals(d)) {
                    denIt.remove();
                    numIt.remove();
                }
            }
        }
        return this;
    }

    @Override
    AbExp der(Variable v) {
        return null;
    }



    @Override
    AbExp set(Variable v, AbExp x) {
        return null;
    }

    @Override
    public String stamp(AbExp abExp) {
        return "(" + num + ")divide(" + den + ")";
    }

}
