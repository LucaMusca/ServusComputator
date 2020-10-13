package parser;


public class Main {

    public static void main(String[] args) {
        var x = Parser.newVariable("x");
        var y = Parser.newVariable("y");
        var c = Parser.newVariable("c");
        var r = Parser.newVariable("radius");
        var d = Parser.newVariable("d");
        var der = Parser.newDifferentialOperator("d_dx", x);
        var l = "c*ln(x)+d";
        //var e = AbExp.toAbExp(s).group().der(x).der(x).simplify();//.set(x,1);
        var e2 = AbExp.toAbExp(String.format("d_dx(d_dx(%s))", l));
        // e.put(AbNum.Num(Utils.fact(20L)),-1);
        System.out.println(e2);


    }
}
