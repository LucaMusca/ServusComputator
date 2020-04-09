package parser;


public class Main {

    public static void main(String[] args) {
        var x = Parser.newVariable("x");
        var y = Parser.newVariable("y");
        var r = Parser.newVariable("r");


        var s = "";
        var e = AbExp.toAbExp(s);
        e = e.group().simplify();
        // System.out.println(e.group().simplify().der(x).simplify());
        System.out.println(e);
        System.out.println();
    }
}
