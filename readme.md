# Symbolic expressions evaluation in Java
## Example usage
You can create new variables using `Parser::newVariable`.

```Java
var x = Parser.newVariable("x");
var y = Parser.newVariable("y");
var alpha = Parser.newVariable("alpha");
```
And then use them in expressions
```
AbExp distance = AbExp.toAbExp("sqrt(x^2 + y^2)");
AbExp e = AbExp.toAbExp("sin(alpha) + x");
```
Once you have a `AbExp`, you can carry out several operations on it, like expression evaluations or derivatives.
```Java
e.set(alpha,distance)
>>> sin(sqrt(x^2 + y^2)) + x
distance.set(x,3).set(y.4).simplify()
>>> 5
distance.der(x).group().simplify()
>>> x / sqrt(x^2 + y^2)
```
`Utils` class contains additional functionalities, like Taylor Expansions or a simple Newton solver.
```Java
eq = AbExp.toAbExp("x - cos(x)");
Utils.newtonSolve(eq,x,0,20)
>>> 0.7390851332151607
eq.taylorExpand(x,0,3)
>>> -1 + 1/2 x^2 + x
```
