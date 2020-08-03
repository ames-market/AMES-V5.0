
package amesmarket.filereaders;

import java.io.File;

import amesmarket.AMESMarketException;

/**
 * An exception to represent a problem in the data file.
 *
 *
 */
public class BadDataFileFormatException extends AMESMarketException {

    public BadDataFileFormatException(){
        super();
    }

    public BadDataFileFormatException(String invalidLine) {
        super(invalidLine);
    }

    public BadDataFileFormatException(int lineNum, String invalidLine){
        super("LINE " + lineNum + ": " + invalidLine);
    }

    public BadDataFileFormatException(int lineNum, String invalidLine, Throwable cause){
        super("LINE " + lineNum + ": " + invalidLine, cause);
    }

    public BadDataFileFormatException(int lineNum, int column, String invalidLine){
        super("LINE " + lineNum + ", COLUMN " + column + " : " + invalidLine);
    }

    /**
     * A BadDataFileFormatException that names the file where the problem occured.
     * @param file
     * @param lineNum
     * @param invalidLine
     */
    public BadDataFileFormatException(File file, int lineNum, String invalidLine) {
        this(lineNum,
                (file != null ? file.getPath() + ": ": "") + invalidLine);
    }

    public BadDataFileFormatException(File file, int lineNum, String invalidLine, Throwable cause) {
        super("LINE " + lineNum + ": " +
                (file != null ? file.getPath() + ": " : "") + invalidLine,
                cause);
    }

    public BadDataFileFormatException(File file, int lineNum, Throwable cause) {
        super("LINE " + lineNum + ": " +
                (file != null ? file.getPath() + ": " : ""),
                cause);
    }

    public BadDataFileFormatException(Throwable cause) {
        super(cause);
    }

    /**
     * Create a "Expected something Found something else" error message.
     * @param linenum
     * @param expected
     * @param found
     * @return
     */
    public static BadDataFileFormatException createUnexpectedString(int linenum, String expected, String found){
        return new BadDataFileFormatException(linenum, "Expected " + expected + ". Found " + found);
    }
}
