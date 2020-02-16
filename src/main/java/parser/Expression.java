package parser;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class UnknownToken extends RuntimeException {
    UnknownToken(String s) {
        super("\"" + s + "\"");
    }
}

public class Expression extends Token {
    public List<Token> tokens;

    public Expression(List<Token> list) {
        tokens = list;
        nestDel();
    }

    private Expression(Boolean bool, List<Token> list) {
        tokens = list;
        if (bool)
            nestDel();
    }

    private Expression(boolean bool, Token... tokens) {
        this(bool, new LinkedList<>(Arrays.asList(tokens)));
    }

    public Expression(Token... tokens) {
        this(new LinkedList<>(Arrays.asList(tokens)));
    }

    public Expression(String s) {
        tokens = Parser.toTokens(s);
        nestDel();
        nestFun();
    }

    @Override
    public String toString() {
        return tokens.toString();
    }

    @Override
    public String print() {
        if (tokens.size() == 1) {
            return tokens.get(0).print() + " ";
        }
        StringBuilder s = new StringBuilder();
        tokens.forEach(t -> {
            if (t instanceof Expression) {
                if (((Expression) t).tokens.size() == 1)
                    s.append(t.print());
                else
                    s.append("(").append(t.print()).append(") ");
            } else {
                s.append(t.print()).append(" ");
            }
        });
        return s.toString();
    }

    @Override
    public Token unNest() {
        if (tokens.size() == 1) {
            return tokens.get(0).unNest();
        }
        List<Token> s = new LinkedList<>();
        tokens.forEach(t -> {
            if (t instanceof Expression) {
                if (((Expression) t).tokens.size() == 1)
                    s.add(t.unNest());
                else
                    s.add(new Expression(t.unNest()));
            } else {
                s.add(t.unNest());
            }
        });
        return new Expression(s);
    }


    List<Token> nestSubExp(int from, int to, Token exp) {
        List<Token> end = tokens.subList(to + 1, tokens.size());
        if (exp instanceof Expression) {
            ((Expression) exp).nestDel();
        }

        List<Token> list = new LinkedList<>(tokens.subList(0, from));
        list.add(exp);
        list.addAll(end);

        return list;
    }

    private boolean check(int b, boolean open) {
        if (b > 0 || !open)
            return false;
        if (b == 0)
            return true;

        throw new UnexpectedToken(Right.rightDelimiter);
    }

    public Expression nest() {
        if (tokens.size() == 1 && tokens.get(0) instanceof Expression) {
            ((Expression) tokens.get(0)).nest();
        }
        nestDel();
        nestFun();
        nestNotAr();
        nestAr();
        return this;
    }

    public void nestAr() {
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t instanceof Minus || t instanceof Plus) {
                if (i == tokens.size() - 1)
                    throw new UnexpectedToken(t);
                if (i == 0) {
                    tokens.add(0, new Number(0)); //if the operator is in the first position
                    i++;
                }
                if (tokens.get(i + 1) instanceof Expression)
                    ((Expression) tokens.get(i + 1)).nest();
                if (tokens.get(i - 1) instanceof Expression)
                    ((Expression) tokens.get(i - 1)).nest();
                Token result = new Expression(tokens.get(i - 1), tokens.get(i), tokens.get(i + 1));
                tokens = nestSubExp(i - 1, i + 1, result);
                i--;
            }
        }
    }

    public void nestNotAr() {

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t instanceof Operator && !(t instanceof Plus) &&  !(t instanceof Minus)) {
                if (i == tokens.size() - 1)
                    throw new UnexpectedToken(t);
                if (i == 0) {
                    tokens.add(0, new Number(0)); //if the operator is in the first position
                    i++;
                }
                if (tokens.get(i + 1) instanceof Expression)
                    ((Expression) tokens.get(i + 1)).nest();
                if (tokens.get(i - 1) instanceof Expression)
                    ((Expression) tokens.get(i - 1)).nest();
                Token result = new Expression(tokens.get(i - 1), tokens.get(i), tokens.get(i + 1));
                tokens = nestSubExp(i - 1, i + 1, result);
                i--;
            }
        }
    }

    public void nestFun() {
        for (int i = tokens.size() - 1; i >= 0; i--) {
            if (tokens.get(i) instanceof Function) {
                if (i == tokens.size() - 1)
                    throw new UnexpectedToken(tokens.get(i));
                if (tokens.get(i + 1) instanceof Expression)
                    ((Expression) tokens.get(i + 1)).nest();
                tokens.set(i, new Expression(false, tokens.get(i), tokens.get(i + 1)));
                tokens.remove(i + 1);
                //   firstLeft++;
            }
        }

    }

    public void nestDel() {
        boolean open = false;
        int firstLeft = 0;
        int brackets = 0;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i) instanceof Right)
                brackets--;
            if (tokens.get(i) instanceof Left) {
                brackets++;
                if (!open) {
                    open = true;
                    firstLeft = i;
                }
            }
            if (check(brackets, open)) {
                tokens = nestSubExp(firstLeft, i, new Expression(tokens.subList(firstLeft + 1, i)));
                i = firstLeft;
                open = false;
            }
        }
        if (brackets != 0)
            throw new UnexpectedToken(Left.leftDelimiter);

    }

}
