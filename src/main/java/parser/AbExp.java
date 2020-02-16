package parser;

public abstract class AbExp {

    public static AbExp toAbExp(String string) {
        return toAbExp(new Expression(string));
    }

    static AbExp toAbExp(Token token) {
        if (token instanceof Expression) {
            var e = (Expression) token;
            e.nest();
            return toAbExpCore(e);
        } else {
            if(token instanceof parser.Number) {
                final parser.Number token1 = (parser.Number) token;
                return AbNum.Num(token1.getValue());
            }
            if(token instanceof Constant) {
                final Constant c = (Constant) token;
                return new AbCost(c);
            }
            if(token instanceof Variable) {
                return new AbVar(((Variable) token));
            }
        }
        throw new UnexpectedToken(token);
    }
    static private AbExp toAbExpCore(Token token) {
        if (token instanceof Expression) {
            var e = ((Expression) token);
            //   e.unNest();
            var tokens = e.tokens;
            try {
                switch (tokens.size()) {
                    case 3:
                        return new AbOp(toAbExpCore(tokens.get(0)), (Operator) tokens.get(1), toAbExpCore(tokens.get(2)));
                    case 2:
                        return new AbFun((Function) tokens.get(0), toAbExpCore(tokens.get(1)));
                    case 1:
                        var t = tokens.get(0);
                        if (t instanceof Expression)
                            return toAbExpCore(t);
                        if (t instanceof Variable)
                            return new AbVar((Variable) t);
                        if (t instanceof parser.Number)
                            return AbNum.Num(((parser.Number) t).getValue());
                        if (t instanceof Constant)
                            return new AbCost(((Constant) t));
                    default:
                        throw new RuntimeException("Something wrong: size mismatch");
                }
            } catch (ClassCastException c) {
                throw new UnexpectedToken(tokens.get(0));
            }
        } else {
            if (token instanceof parser.Number)
                return AbNum.Num(((parser.Number) token).getValue());
            if (token instanceof Constant)
                return new AbCost((Constant) token);
            if (token instanceof Variable)
                return new AbVar((Variable) token);
        }

        throw new UnexpectedToken(token);
    }

    abstract double eval(double... in);
    abstract AbExp der(Variable v);
    abstract AbExp simplify();

    abstract AbExp set(Variable v, double x);
    abstract AbExp set(Variable v, AbExp x);

    public boolean is(double x){
        return (this instanceof AbNum) && ((AbNum) this).getValue()== x;
    }
    public boolean is(Class<? extends Token> c){
        Class<?> t = getClass();
       if(t==AbOp.class){
           return ((AbOp) this).operator.getClass() == c;
       }
        if(t==AbFun.class){
            return ((AbFun) this).function.getClass() == c;
        }
       return false;
    }
    public boolean isArith(){
        return is(Plus.class) || is(Minus.class);
    }

    public AbExp group(){
        if(this.is(Times.class)){
            return Factors.fromTimes( ((AbOp) this).left, ((AbOp) this).right);
        }
        if(this.is(Divide.class)){
            return Factors.fromDivide( ((AbOp) this).left, ((AbOp) this).right);
        }
        //TODO: it doesn't enter in the tree
        return this;
    }

    @Override
    public String toString() {
        return stamp(null);
    }

    public String stamp(AbExp abExp) {  //AbExp is parent
        return super.toString();
    }
}

