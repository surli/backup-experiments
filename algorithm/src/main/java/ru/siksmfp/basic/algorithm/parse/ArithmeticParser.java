package ru.siksmfp.basic.algorithm.parse;

import ru.siksmfp.basic.structure.stack.Stack;

import java.util.regex.Pattern;

/**
 * @author Artem Karnov @date 1/5/2018.
 * artem.karnov@t-systems.com
 */
public class ArithmeticParser {
    private static final char OPENED_PARENTHESES = '(';
    private static final char CLOSED_PARENTHESES = ')';
    private static final char SPACE = ' ';
    private static final Pattern DOUBLE_PATTERN = Pattern.compile(
            "[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)" +
                    "([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|" +
                    "(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))" +
                    "[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*");

    public static void main(String[] args) {
        ArithmeticParser parser = new ArithmeticParser();

        //A + B + C
        //ABC++

        //A*(B+C)
        //ABC+*

        //A+B*(C-D)
        //ABC

        //A*(B+C)*D
        //ABC+D**

//        System.out.println(parser.infixToPostfix("A+B+C")); //A B C + +
//        System.out.println(parser.infixToPostfix("A+B*C")); //A B C * +
//        System.out.println(parser.infixToPostfix("A*(B+C)")); //A B C + *
//        System.out.println(parser.infixToPostfix("A + B * (C - D)")); //A B C D - * +
//        System.out.println(parser.infixToPostfix("A*(B+C)-D/(E+F)")); //A B C + * D E F + / -
//        System.out.println(parser.infixToPostfix("A*(B+C)*D")); //A B C + * D *
//        System.out.println(parser.infixToPostfix("A^B*C")); //A B ^ C *
//        System.out.println(parser.infixToPostfix("(A+B)^C+D")); //A B + C ^ D +

//        System.out.println(parser.calculateExpression("1 2 3 + +")); //6
//        System.out.println(parser.calculateExpression("1 2 3 * +")); //7
//        System.out.println(parser.calculateExpression("1 2 3 4 - * +")); //-1
//        System.out.println(parser.calculateExpression("2 2 3.1 + * 4 1 1 + / -")); //8.2
//        System.out.println(parser.calculateExpression("2 2 ^")); //4
//        System.out.println(parser.calculateExpression("2 2 2 ^ ^")); //16

//        System.out.println(parser.parse("1+1")); //1
//        System.out.println(parser.parse("1+1+1")); //3
//        System.out.println(parser.parse("1+(1+1)")); //3
//        System.out.println(parser.parse("1^(1+1)")); //1 isn't cor

//        System.out.println(parser.parse("1+2+1+1"));
        System.out.println(parser.parse("8-2-2"));
//        System.out.println(parser.parse("(1+2)+(1+1)"));
//        System.out.println(parser.parse("2*2+1"));
//        System.out.println(parser.parse("2*2+1+1"));
//        System.out.println(parser.parse("2/2+1/1"));
//        System.out.println(parser.parse("2^9"));
//        System.out.println(parser.parse("(2+2)^2-1"));
//        System.out.println(parser.parse("(1+(2+1)+1)"));

    }

    //to postfix notation
    //if current element is operand - to string
    //if current element is operator
    //if stack is empty  - to stack
    // if not - stack.peek > element
    //yes - element to stack
    //no - stack.pop to string by stack.pop==stack.pop


    //calculate
    //Get first, Get second to stack1
    // if third is operator - calculate and result to stack2
    // third is operand - add it to stack1
    //брать с конца опернанты, с начала операторы.  сопоставлять


    public double parse(String arithmeticString) {
        Stack<Double> operands = new Stack<>();
        Stack<Character> operators = new Stack<>();
        for (char current : arithmeticString.toCharArray()) {
            if (getOperatorPriority(current) == 0) {
                operands.push(Double.parseDouble(String.valueOf(current)));
            } else if (operators.isEmpty()) {
                operators.push(current);
            } else if (current == ')') {
                do {
                    operands.push(makeArithmeticOperation(operands.pop(), operands.pop(), operators.pop()));
                } while (operators.peek() != '(');
                operators.pop();
            } else if (getOperatorPriority(operators.peek()) <= getOperatorPriority(current)) {
                operators.push(current);
            } else if (operators.peek() == '(') {
                operators.push(current);
            } else {
                do {
                    operands.push(makeArithmeticOperation(operands.pop(), operands.pop(), operators.pop()));
                } while (!operators.isEmpty() && getOperatorPriority(current) >= getOperatorPriority(operators.peek()));
                operators.push(current);
            }
        }
        do {
            operands.push(makeArithmeticOperation(operands.pop(), operands.pop(), operators.pop()));
        } while (!operators.isEmpty());
        return operands.pop();
    }

    private String infixToPostfix(String arithmeticString) {
        Stack<Character> operators = new Stack<>();
        StringBuilder result = new StringBuilder();
        for (char current : arithmeticString.toCharArray()) {
            int currentPriority = getOperatorPriority(current);
            if (getOperatorPriority(current) == 0) {
                result.append(current).append(' ');
            } else {
                if (operators.isEmpty()) {
                    operators.push(current);
                } else {
                    if (getOperatorPriority(operators.peek()) > currentPriority) {
                        operators.push(current);
                    } else {
                        result.append(operators.pop()).append(' ');
                    }
                }
            }
        }

        return result.toString();
    }

    private double makeArithmeticOperation(double firstValue, double secondValue, char operator) {
        switch (operator) {
            case '^':
                return Math.pow(secondValue, firstValue);
            case '*':
                return firstValue * secondValue;
            case '/':
                return secondValue / firstValue;
            case '+':
                return firstValue + secondValue;
            case '-':
                return secondValue - firstValue;
            default:
                throw new ArithmeticException("Unknown operator");
        }
    }

    private int getOperatorPriority(char operator) {
        switch (operator) {
            case '(':
            case ')':
                return 4;
            case '^':
                return 3;
            case '*':
            case '/':
                return 2;
            case '+':
            case '-':
                return 1;
            default:
                return 0;
        }
    }

    public static boolean isDouble(String s) {
        return DOUBLE_PATTERN.matcher(s).matches();
    }
}
