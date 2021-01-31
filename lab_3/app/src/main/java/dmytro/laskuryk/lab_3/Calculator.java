package dmytro.laskuryk.lab_3;

import java.util.LinkedList;

public class Calculator {
    public static Double eval(String s) {
        LinkedList<Double> numbers = new LinkedList<Double>();
        LinkedList<Character> operators = new LinkedList<Character>();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (isOperator(c)) {
                while (!operators.isEmpty() && getPriority(operators.getLast()) >= getPriority(c)) {
                    processOperator(numbers, operators.removeLast());
                }
                operators.add(c);
            } else {
                String operand = "";
                while (i < s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.'))
                    operand += s.charAt(i++);
                --i;
                numbers.add(Double.parseDouble(operand));
            }
        }

        while (!operators.isEmpty()) {
            processOperator(numbers, operators.removeLast());
        }

        Double rounded = Math.round(numbers.getFirst() * 100000.0) / 100000.0;
        return rounded;
    }

    static Boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    static Integer getPriority(char op) {
        switch (op) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return -1;
        }
    }

    static void processOperator(LinkedList<Double> st, char op) {
        Double r = st.removeLast();
        Double l = st.removeLast();

        switch (op) {
            case '+':
                st.add(l + r);
                break;
            case '-':
                st.add(l - r);
                break;
            case '*':
                st.add(l * r);
                break;
            case '/':
                if (r == 0) {
                    throw new ArithmeticException();
                }

                st.add(l / r);
                break;
        }
    }
}
