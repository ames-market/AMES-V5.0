package amesmarket;

import java.util.regex.Pattern;

/**
 * A slightly more intelligent number parser than just calling Integer.parseInt
 * or Support.parseDouble. For example, it can check to see if a string is a
 * floating point, and cast it to an int if required.
 *
 *
 */
public class NumberRecognizer {

    private final Pattern intPattern;
    private final Pattern fpPattern;

    public NumberRecognizer() {
        final String intRegex = "[\\+-]?\\d+";

        //FROM Sun/Oracle docs
        //http://docs.oracle.com/javase/6/docs/api/java/lang/Double.html#valueOf%28java.lang.String%29
        final String Digits = "(\\p{Digit}+)";
        final String HexDigits = "(\\p{XDigit}+)";
        // an exponent is 'e' or 'E' followed by an optionally
        // signed decimal integer.
        final String Exp = "[eE][+-]?" + Digits;
        final String fpRegex = ("[\\x00-\\x20]*" + // Optional leading "whitespace"
                "[+-]?(" + // Optional sign character
                "NaN|" + // "NaN" string
                "Infinity|" + // "Infinity" string

                // A decimal floating-point string representing a finite positive
                // number without a leading sign has at most five basic pieces:
                // Digits . Digits ExponentPart FloatTypeSuffix
                //
                // Since this method allows integer-only strings as input
                // in addition to strings of floating-point literals, the
                // two sub-patterns below are simplifications of the grammar
                // productions from the Java Language Specification, 2nd
                // edition, section 3.10.2.

                // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

        // . Digits ExponentPart_opt FloatTypeSuffix_opt
                "(\\.(" + Digits + ")(" + Exp + ")?)|" +

                // Hexadecimal strings
                "((" +
                // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                "(0[xX]" + HexDigits + "(\\.)?)|" +

                // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                ")[pP][+-]?" + Digits + "))" + "[fFdD]?))" + "[\\x00-\\x20]*");// Optional trailing "whitespace"

        intPattern = Pattern.compile(intRegex);
        fpPattern = Pattern.compile(fpRegex);
    }

    /**
     * Helper method. Capture common behavior for
     * {@link #isFloatingPoint(String)} {@link #intPattern} .
     *
     * @param p
     * @param s
     * @return
     */
    private boolean checkMatch(Pattern p, String s) {
        return p.matcher(s).matches();
    }

    /**
     * Check if the string can be parsed as floating point/decimal number.
     *
     * @param s
     * @return
     */
    public boolean isFloatingPoint(String s) {
        return checkMatch(fpPattern, s);
    }

    /**
     * Check if the string can be parsed as an int.
     *
     * @param s
     * @return
     */
    public boolean isInt(String s) {
        return checkMatch(intPattern, s);
    }

    /**
     * Convert s to an double, if possible..
     *
     * @param s
     * @return s as an int value.
     * @throws NumberFormatException if s does not represent an int or floating
     *             point.
     */
    public double stod(String s) {
        if (isFloatingPoint(s)) {
            return Support.parseDouble(s);
        } else {
            throw new NumberFormatException(s + " cannot be parsed as a double");
        }
    }

    /**
     * Convert s to an int, if possible. If s represents a floating point type
     * value, parse as double and cast as an int.
     *
     * @param s
     * @return s as an int value.
     * @throws NumberFormatException if s does not represent an int or floating
     *             point.
     */
    public int stoi(String s) {
        if (isInt(s)) {
            return Integer.parseInt(s);
        } else if (isFloatingPoint(s)) {
            return (int) Support.parseDouble(s);
        } else {
            throw new NumberFormatException(s + " cannot be parsed as an int");
        }
    }
}
