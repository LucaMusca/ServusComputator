package parser;


public class Main {

    public static void main(String[] args) {
        var x = Parser.newVariable("x");
        var y = Parser.newVariable("y");
        var r = Parser.newVariable("r");

        var s = "exp(-x*sqrt(1-x^2))" ;

        var e = AbExp.toAbExp(s);

        var e2= AbExp.toAbExp(s);
        System.out.println(e.group());

      // System.out.println(e.der(r).set(r,0).simplify());

    }
}
