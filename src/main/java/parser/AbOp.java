package parser;

import java.util.Objects;

public class AbOp extends AbExp {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbOp)) return false;
        AbOp abOp = (AbOp) o;
        return left.equals(abOp.left) &&
                right.equals(abOp.right) &&
                operator.equals(abOp.operator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right, operator);
    }

    AbExp left;
    AbExp right;
    Operator operator;

    public AbOp(AbExp left, Operator operator, AbExp right) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public String stamp(AbExp abExp) {
        if(abExp!= null &&!abExp.isArith() && isArith())
            return String.format("(%s %s %s)", left.stamp(this), operator.toString(), right.stamp(this));
        else
            return String.format("(%s %s %s)", left.stamp(this), operator.toString(), right.stamp(this));
    }

    @Override
    public AbExp copy() {
        return new AbOp(left.copy(),operator,right.copy());
    }

    @Override
    double eval(double... in) {
        return operator.operate(left.eval(in),right.eval(in));
    }

    AbExp der(Variable v) {
        return operator.derivative.fun(left,right,v);
    }

    @Override
    AbExp simplify() {
        left = left.simplify();
        right = right.simplify();
        if(left instanceof AbNum && right instanceof AbNum) { //TODO fix combination of divide and times
            return AbNum.Num(operator.operate(((AbNum) left).getValue(),((AbNum) right).getValue()));
        }
        return operator.simplify(left,right);
    }

     @Override
     AbExp set(Variable v, double x) {
         left = left.set(v,x);
         right = right.set(v,x);
         return this;
     }

     @Override
     AbExp set(Variable v, AbExp x) {
         left = left.set(v,x);
         right = right.set(v,x);
         return this;
     }

     public AbExp group(){
         if(this.is(Plus.class)){
             return AddendsMap.merge( new AddendsMap(this.left.group()),
                     new AddendsMap(this.right.group()));
         }
         if(this.is(Minus.class)){
             return AddendsMap.merge( new AddendsMap(this.left.group()),
                     AddendsMap.invert(new AddendsMap(this.right.group())));
         }
         if(this.is(Times.class)){
             return FactorsMap.merge( new FactorsMap(this.left.group()),
                     new FactorsMap(this.right.group()));
         }
         if(this.is(Pow.class)){
            return new FactorsMap(this);
         }
         if(this.is(Divide.class)){
             return FactorsMap.merge( new FactorsMap(this.left.group()),
                     FactorsMap.invert(new FactorsMap(this.right.group())));
         }

         left = left.group();
         right = right.group();
         return this;
     }

   /*  @Override
    public AbExp group() {
        if(this.is(Times.class)){
            return Factors.fromTimes( ((AbOp) this).left, ((AbOp) this).right);
        }
        if(this.is(Divide.class)){
            return Factors.fromDivide( ((AbOp) this).left, ((AbOp) this).right);
        }
        if(this.is(Plus.class)){
            return Addends.fromPlus( ((AbOp) this).left, ((AbOp) this).right);
        }
        if(this.is(Minus.class)){
            return Addends.fromMinus( ((AbOp) this).left, ((AbOp) this).right);
        }
        left = left.group();
        right = right.group();
        //TODO: pow
        return this;
    }*/
}
