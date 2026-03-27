import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class Funcion {

    public static double evaluar(String expresion, double x) {
        String texto = expresion.replaceAll("\\s+", "");
        List<Token> tokens = tokenize(texto);
        List<Token> rpn = shuntingYard(tokens);
        return evalRPN(rpn, x);
    }

    public static DoubleUnaryOperator crearFuncion(String expresion) {
        return y -> evaluar(expresion, y);
    }

    private static List<Token> tokenize(String s) {
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < s.length();) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }
            if (Character.isDigit(c) || c == '.') {
                int j = i + 1; while (j < s.length() && (Character.isDigit(s.charAt(j)) || s.charAt(j) == '.')) j++;
                tokens.add(new Token(TokenType.NUMBER, s.substring(i, j)));
                i = j; continue;
            }
            if (Character.isLetter(c)) {
                int j = i + 1; while (j < s.length() && Character.isLetter(s.charAt(j))) j++;
                String name = s.substring(i, j).toLowerCase();
                if (name.equals("x")) tokens.add(new Token(TokenType.VARIABLE, "x"));
                else tokens.add(new Token(TokenType.FUNCTION, name));
                i = j; continue;
            }
            if (c == '(') { tokens.add(new Token(TokenType.LEFT_PAREN, "(")); i++; continue; }
            if (c == ')') { tokens.add(new Token(TokenType.RIGHT_PAREN, ")")); i++; continue; }
            if ("+-*/^".indexOf(c) >= 0) {
                String op = String.valueOf(c);
                if (c == '-' && (tokens.isEmpty() || tokens.get(tokens.size() - 1).type == TokenType.OPERATOR || tokens.get(tokens.size() - 1).type == TokenType.LEFT_PAREN))
                    op = "u"; 
                tokens.add(new Token(TokenType.OPERATOR, op));
                i++; continue;
            }
            throw new IllegalArgumentException("Carácter no válido en la expresión: " + c);
        }
        return tokens;
    }

    private static List<Token> shuntingYard(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        List<Token> stack = new ArrayList<>();

        for (Token t : tokens) {
            switch (t.type) {
                case NUMBER, VARIABLE -> output.add(t);
                case FUNCTION -> stack.add(t);
                case OPERATOR -> {
                    while (!stack.isEmpty() && stack.get(stack.size()-1).type == TokenType.OPERATOR) {
                        Token top = stack.get(stack.size()-1);
                        if ((isLeftAssociative(t.value) && precedence(t.value) <= precedence(top.value))
                            || (!isLeftAssociative(t.value) && precedence(t.value) < precedence(top.value))) {
                            output.add(stack.remove(stack.size()-1));
                        } else break;
                    }
                    stack.add(t);
                }
                case LEFT_PAREN -> stack.add(t);
                case RIGHT_PAREN -> {
                    while (!stack.isEmpty() && stack.get(stack.size()-1).type != TokenType.LEFT_PAREN)
                        output.add(stack.remove(stack.size()-1));
                    if (stack.isEmpty() || stack.get(stack.size()-1).type != TokenType.LEFT_PAREN)
                        throw new IllegalArgumentException("Paréntesis desbalanceados");
                    stack.remove(stack.size()-1);
                    if (!stack.isEmpty() && stack.get(stack.size()-1).type == TokenType.FUNCTION)
                        output.add(stack.remove(stack.size()-1));
                }
            }
        }

        while (!stack.isEmpty()) {
            Token t = stack.remove(stack.size()-1);
            if (t.type == TokenType.LEFT_PAREN || t.type == TokenType.RIGHT_PAREN)
                throw new IllegalArgumentException("Paréntesis desbalanceados");
            output.add(t);
        }

        return output;
    }

    private static double evalRPN(List<Token> tokens, double x) {
        List<Double> stack = new ArrayList<>();
        for (Token t : tokens) {
            switch (t.type) {
                case NUMBER -> stack.add(Double.parseDouble(t.value));
                case VARIABLE -> stack.add(x);
                case OPERATOR -> {
                    if (t.value.equals("u")) {
                        double v = stack.remove(stack.size()-1);
                        stack.add(-v);
                    } else {
                        double b = stack.remove(stack.size()-1);
                        double a = stack.remove(stack.size()-1);
                        stack.add(applyOperator(a,b,t.value));
                    }
                }
                case FUNCTION -> stack.add(applyFunction(t.value, stack.remove(stack.size()-1)));
                default -> throw new IllegalStateException("Token inesperado: " + t);
            }
        }
        if (stack.size() != 1) throw new IllegalArgumentException("Expresión inválida");
        return stack.get(0);
    }

    private static double applyOperator(double a, double b, String op) {
        return switch(op) {
            case "+" -> a+b;
            case "-" -> a-b;
            case "*" -> a*b;
            case "/" -> a/b;
            case "^" -> Math.pow(a,b);
            default -> throw new IllegalArgumentException("Operador desconocido: "+op);
        };
    }

    private static double applyFunction(String fn, double v) {
        return switch(fn) {
            case "sin" -> Math.sin(v);
            case "cos" -> Math.cos(v);
            case "tan" -> Math.tan(v);
            case "sqrt" -> Math.sqrt(v);
            case "log" -> Math.log(v);
            case "exp" -> Math.exp(v);
            case "abs" -> Math.abs(v);
            case "asin" -> Math.asin(v);
            case "acos" -> Math.acos(v);
            case "atan" -> Math.atan(v);
            default -> throw new IllegalArgumentException("Función desconocida: "+fn);
        };
    }

    private static int precedence(String op) {
        return switch(op) {
            case "u" -> 4;
            case "^" -> 3;
            case "*","/" -> 2;
            case "+","-" -> 1;
            default -> 0;
        };
    }

    private static boolean isLeftAssociative(String op) {
        return !op.equals("^") && !op.equals("u");
    }

    private enum TokenType { NUMBER, VARIABLE, OPERATOR, FUNCTION, LEFT_PAREN, RIGHT_PAREN }

    private static class Token {
        public final TokenType type;
        public final String value;
        Token(TokenType type, String value) { this.type=type; this.value=value; }
        @Override public String toString() { return "{" + type + ":" + value + "}"; }
    }
}