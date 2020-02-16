package parser;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {

    static final int MAX_LENGTH;
    static Map<String, Token> knownTokens;
    public static List<Variable> variables;

    static Operator minus = new Minus();
    static Delimiter left = new Left("(");
    static Delimiter right = new Right(")");
    static Operator plus = new Plus();
    public static Operator times = new Times();
    static Operator divide = new Divide();
    //make singleton classes
    static Operator pow = new Pow();
    static Function sin = new Sin();
    static Function cos = new Cos();
    static Function exp = new Exp();
    static Function ln = new Ln();
    static Function mn = new MinFun(); // instantiated to have it as a singleton: it's not put in knownToken, not to clash with "-"
    static Function sqrt = new Sqrt();

    static {

        List<Token> tokenList = List.of(
                left,
                right,
                plus,
                minus,
                times,
                divide,
                pow,
                new Constant("pi", Math.PI),
                new Constant("e", Math.E),
                sin,
                cos,
                exp,
                ln,
                sqrt
        );
        knownTokens = mapFromList(tokenList);
        variables = new ArrayList<>();
        //  MAX_LENGTH = knownTokens.entrySet().stream().min(Map.Entry.comparingByKey()).get().getKey().length();
        Optional<Map.Entry<String, Token>> max = knownTokens.entrySet().stream().max(Map.Entry.comparingByKey());
        MAX_LENGTH = max.map(stringTokenEntry -> stringTokenEntry.getKey().length()).orElse(0);
    }

    public static Variable newVariable(String s){
        var v = new Variable(s);
        knownTokens.put(s, v);
        variables.add(v);
        return v;
    }

    private static Map<String, Token> mapFromList(List<Token> tokenList) {
        return tokenList.stream().collect(Collectors.toMap(Token::getString, t -> t, (a, b) -> b));
    }

    static class Tuple {
        String string;
        List<Token> tokens;

        public Tuple(String string, List<Token> tokens) {
            this.string = string;
            this.tokens = tokens;
        }

        public List<Token> getTokens() {
            return tokens;
        }
    }

    static class Chunk {
        String string;
        Boolean isNumber;

        public Chunk(String string, Boolean isNumber) {
            this.string = string;
            this.isNumber = isNumber;
        }

        public String toString() {
            return string;
        }
    }

    static class NotFoundException extends RuntimeException {
    }

    static class AmbiguousExpressionException extends RuntimeException {
        public AmbiguousExpressionException(Object solution) {
            super(solution.toString());
        }
    }

    static void toChunkCore(String s, List<Chunk> chunkList) {
        if (s.length() > 0) {
            int i;
            for (i = 0; i < s.length(); i++) {
                if (!((Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')))
                    break;
            }
            if (i == 0) { //not a number
                int j;
                for (j = 0; j < s.length(); j++) {
                    if (Character.isDigit(s.charAt(j)) || s.charAt(j) == '.')
                        break;
                }
                chunkList.add(new Chunk(s.substring(0, j), false));
                toChunkCore(s.substring(j), chunkList);
            } else {  //is a number
                chunkList.add(new Chunk(s.substring(0, i), true));
                toChunkCore(s.substring(i), chunkList);
            }
        }
    }

    static List<Chunk> toChunk(String s) {
        List<Chunk> list = new LinkedList<>();
        toChunkCore(s, list);
        return list;
    }

    // function to be called
    static List<Token> toTokens(String s) {
        List<Token> tokenList = new LinkedList<>();
        s = s.replaceAll(" ", "");
        if(s.length()==0)
            throw new RuntimeException("Input is empty");
        List<Chunk> chunkList = toChunk(s);
        for (Chunk c : chunkList) {
            if (c.isNumber)
                try {
                    tokenList.add(new Number(Double.parseDouble(c.string)));
                } catch (NumberFormatException exc) {
                    throw new UnknownToken(c.string);
                }
            else {
                try {
                    tokenList.addAll(parseToken(c.string));
                } catch (NotFoundException e) {
                    throw new UnknownToken(c.string);
                }
            }
        }
        return tokenList;
    }

    private static List<Token> parseToken(String s) throws NotFoundException {
        List<List<Token>> solution = new LinkedList<>();
        parseToken(new Tuple(s, new LinkedList<>()), solution);
        if (solution.size() > 1)
            throw new AmbiguousExpressionException(solution);
        if (solution.size() == 0)
            throw new UnknownToken(s);
        return solution.get(0);
    }

    private static void parseToken(Tuple tuple, List<List<Token>> solution) throws NotFoundException {
        String string = tuple.string;
        List<Token> list = tuple.getTokens();
        if (string.length() == 0) {
            solution.add(list);
            return;
        }

        var possTokens = knownTokens.entrySet().stream().filter(stringTokenEntry -> string.startsWith(stringTokenEntry.getKey()))
                .collect(Collectors.toList());
        if (possTokens.size() == 0)
            throw new NotFoundException();
        for (Map.Entry<String, Token> p : possTokens) {
            try {
                parseToken(new Tuple(string.substring(p.getKey().length()), appendAndReturn(list, p.getValue())), solution);
            } catch (NotFoundException ignored) {
            }
        }
    }

    private static <T> List<T> appendAndReturn(List<T> list, T args) {
        return Stream.concat(list.stream(), Stream.of(args))
                .collect(Collectors.toList());
    }



}
