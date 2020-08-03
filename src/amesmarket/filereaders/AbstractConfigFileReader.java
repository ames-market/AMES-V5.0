
package amesmarket.filereaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Scanner;

import amesmarket.Support;

/**
 * Abstract reader for any file reader which
 * is line based and uses "//" as a comment character.
 *
 * Any reader extending this class overrides the 'read'
 * method (with out arguments) to define how to read
 * each line in the file.
 *
 * The reader is reentrent, meaning that the same reader object can
 * be used to read multiple config files.
 *
 * Classes which extend the {@link AbstractConfigFileReader} must
 * implement the {@link #read()} method, which is is the hook for reading
 * the config file.
 *
 * The {@link #move} method should be called every time a new line of input is
 * required.
 *
 *
 * @param <T> Type of object returned by the read method.
 */
public abstract class AbstractConfigFileReader<T> {

    /**
     * White space regular expression
     */
    protected static final String WS_REG_EX = "\\s+";


    private Scanner loadProfileReader;
    private String commentMarker = "//";

    /**
     * Stores the most recently read line of the file.
     */
    protected String currentLine = null;

    /**
     * Common description description string for unexpected end of file.
     */
    protected static final String UNEXPECTED_EOF = "Unexpected end of file";

    /**
     * The current line number of the input file.
     */
    protected int lineNum = 0;

    /**
     * Track the source file, if used.
     *
     * If a the source file is not null, all scenario files and
     * the expected load file should be relative to the location of
     * this file.
     */
    protected File sourceFile = null;

    /**
     * Hook method that drives the rest of the reading of the file.
     * @return
     */
    protected abstract T read(int interval) throws BadDataFileFormatException;

    /**
     * Read the Scenario from the specified file.
     *
     * @param file input file to read from
     * @return A object that represents the data in the scenario file.
     * @throws FileNotFoundException
     * @throws BadDataFileFormatException
     */
    public T read(final File file, int interval) throws BadDataFileFormatException {
        if (file == null) {
            throw new IllegalArgumentException(
                    "Load profile file may not be null");
        }

        try {
            this.sourceFile = file;
            initialize(new Scanner(file));
            return read(interval);
        } catch(FileNotFoundException fnfe){
            throw new BadDataFileFormatException("Could not find " + file.getPath());
        } catch (IllegalArgumentException il) {
            throw new BadDataFileFormatException(lineNum, currentLine, il);
        } finally {
            if(loadProfileReader != null)
                loadProfileReader.close();
        }
    }

    /**
     * Read the scenario from the input reader.
     *
     * @param loadProfileStream Must not be null.
     * @return A object that represents the data in the scenario file.
     * @throws BadDataFileFormatException
     * @throws IllegalArgumentException if loadProfileReader is null.
     */
    public T read(final Reader loadProfileStream, int interval) throws BadDataFileFormatException {
        if(loadProfileStream == null) {
            throw new IllegalArgumentException("InputStream may not be null");
        }

        initialize(new Scanner(loadProfileStream));

        try{
            return read(interval);
        }catch (IllegalArgumentException il) {
            throw new BadDataFileFormatException(lineNum, currentLine, il);
        }
    }

    /**
     * Read the scenario from the input stream.
     *
     * @param loadProfileStream Must not be null.
     * @return A object that represents the data in the scenario file.
     * @throws BadDataFileFormatException
     * @throws IllegalArgumentException if loadProfileReader is null.
     */
    public T read(final InputStream loadProfileStream, int interval) throws BadDataFileFormatException {
        if(loadProfileStream == null) {
            throw new IllegalArgumentException("InputStream may not be null");
        }

        initialize(new Scanner(loadProfileStream));

        return read(interval);
    }

    /**
     * Perform all of the common initialization steps
     * to get ready to read a new file/input stream.
     *
     * And sub types with state that needs to be initialized before
     * reading a file <i>must</i> override this method to reset it's state
     * and <i>must</i> call s.initialize(Scanner) to correctly
     * initialize the parent class's (AbstractConfigFileReader) state.
     * @param s
     */
    private void initialize(Scanner s){
        currentLine = null; //make sure we don't get old data on reuse
        lineNum = 0;
        loadProfileReader = s;
    }

    /**
     * Move to the next line in the file.
     *
     * Trims the whitespace off of the read in line. When the method finishes,
     * it will have set the {@link #currentLine} field to one of two values.
     * Either it will be the next line of the input (stripped of
     * leading/trailing whitespace) or it will be null, indicating the end of
     * the file was reached.
     *
     * The general protocol for using this function, is to call it as soon as
     * we enter a method which needs to read some input.
     *
     * @param failOnEOF if true, throw an exception if the end of file is
     *            encountered.
     * @throws BadDataFileFormatException if EOF encountered and should not have
     *             been.
     */
    protected void move(boolean failOnEOF) throws BadDataFileFormatException {
        currentLine = null;
        if(loadProfileReader.hasNextLine()){
            do{ //read until we have non-blank line, or encounter the end of the file/stream.
                ++lineNum;
                currentLine = trimLine(loadProfileReader.nextLine());
            }while( "".equals(currentLine) && loadProfileReader.hasNextLine() );

            //See if we ran out file while reading for the next
            //non-whitespace only line.
            if( "".equals(currentLine) ){
                currentLine = null;
            }
        }
        else {
            currentLine = null;
       }

        if(currentLine == null && failOnEOF){
            throw new BadDataFileFormatException(sourceFile, lineNum, UNEXPECTED_EOF);
        }
    }

    /**
     * Parse a key/value pair line, where the key and value are seperated by a delim.
     *
     * @param delim regex to split on
     * @param line to split
     * @param trimWhiteSpace whether or not to trim the whitespace.
     * @return the key and value for the pair. White space is removed
     * @throws BadDataFileFormatException
     */
    public String[] splitKeyValue(String delim, String line, boolean trimWhiteSpace) throws BadDataFileFormatException {
        line = line.trim();
        String[] splits = line.split(delim);
        if(splits.length != 2) {
            throw new BadDataFileFormatException("Expected key/value pair in " +
                                                 line + ". Expected 2 items, found " + splits.length);
        }

        if(trimWhiteSpace) {
            Support.trimAllStrings(splits);
        }

        return splits;
    }

    public int stoi(String s) throws BadDataFileFormatException{
        try{
            return Integer.parseInt(s);
        }catch(NumberFormatException nfe){
            throw new BadDataFileFormatException(sourceFile, lineNum, "Expected \"" + s
                    + "\" to be an integer", nfe);
        }
    }

    public double stod(String s) throws BadDataFileFormatException{
        try{
            return Support.parseDouble(s);
        }catch(NumberFormatException nfe){
            throw new BadDataFileFormatException(sourceFile, lineNum, "Expected \"" + s
                    + "\" to be a decimal", nfe);
        }
    }

    /**
     * Remove comments and trim whitespace.
     * @param l
     * @return
     */
    public String trimLine(String l){
        int cStart = l.indexOf(commentMarker);

        if(-1 != cStart){
            l = l.substring(0, cStart);
        }

        return l.trim();
    }
}
