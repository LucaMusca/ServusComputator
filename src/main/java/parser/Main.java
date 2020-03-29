package parser;


public class Main {

    public static void main(String[] args) {
        var x = Parser.newVariable("x");
        var y = Parser.newVariable("y");
        var r = Parser.newVariable(" ");

        var s = "x^2";

        var e = AbExp.toAbExp(s);

       // System.out.println(e.group().simplify().der(x).simplify());
         System.out.println(Utils.taylor(e,x,0,6).simplify());
         System.out.println(Utils.taylorGroup(e,x,0,6).simplify());
    }
}
