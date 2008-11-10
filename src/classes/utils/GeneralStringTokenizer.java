
/*
 * Created on July 10, 2004
 */

package classes.utils;

/**
 * Tokenizes a general String object.<br>
 * The tokens must be separated with the GENERAL_SEPARATOR_CHAR character.
 * The tokenizable string must last with a separator character to be able to gain the last token.
 *
 * @author Andras Belicza
 */
public class GeneralStringTokenizer {

    /** General separator character separating things in strings.              */
    public static final char   GENERAL_SEPARATOR_CHAR   = '|';
    /** General replacer character replacing not allowed separator characters. */
    public static final char   GENERAL_REPLACER_CHAR    = '_';
    /** General separator string separating things in strings (the general separator character in string format). */
    public static final String GENERAL_SEPARATOR_STRING = "" + GENERAL_SEPARATOR_CHAR;

    /** Characters of the tokenizable string.  */
    private final char[] stringChars;
    /** First index of lastly processed token. */
    private int          lastTokenFirstIndex = -1;
    /** Last index of lastly processed token.  */
    private int          lastTokenLastIndex  = -1;

    /**
     * Creates a new GeneralStringTokenizer.
     * @param string the string to be tokenized
     */
    public GeneralStringTokenizer( final String string ) {
        stringChars = string.toCharArray();
    }
	
	/**
	 * Tells whether there are unreturned characters left.<br>
	 * Note: remainingString() always returns those if hasMoreTokens() returns true,
	 * but nextXXXToken() methods only if the tokenized string terminates with GENERAL_SEPARATOR_CHAR.
	 * @return true if there are unreturned characters left; false otherwise
	 */
	public boolean hasRemainingString() {
		return lastTokenLastIndex + 1 < stringChars.length;
	}
    
    /**
     * Returns the next String token.
     * @return the next String token
     */
    public String nextStringToken() {
        lastTokenFirstIndex = lastTokenLastIndex + 1;
        while ( stringChars[ ++lastTokenLastIndex ] != GENERAL_SEPARATOR_CHAR )
            ;
        return new String( stringChars, lastTokenFirstIndex, lastTokenLastIndex - lastTokenFirstIndex );
    }

    /**
     * Returns the next int token.
     * @return the next int token
     */
    public int nextIntToken() {
        return Integer.parseInt( nextStringToken() );
    }

    /**
     * Returns the next boolean token.
     * @return the next boolean token
     */
    public boolean nextBooleanToken() {
        return Boolean.parseBoolean( nextStringToken() );
    }

    /**
     * Returns the remaining string.
     * @return the remaining string
     */
    public String remainingString() {
        final String remainingString_ = new String( stringChars, lastTokenLastIndex + 1, stringChars.length - lastTokenLastIndex - 1 );
        lastTokenLastIndex = lastTokenFirstIndex = stringChars.length - 1;
        return remainingString_;
    }

    /**
     * Checks and corrigates the given string.<br>
     * To corrigate means to replace the general separator character with the neutral replacer character.
     * Since options may be stored in data text files, comment line characters must be also replaced.
     * @param string string to be checked
     * @return the checked/correct string
     */
    public static String checkString( final String string ) {
        return string.replace( GENERAL_SEPARATOR_CHAR              , GENERAL_REPLACER_CHAR )
                     .replace( DataTextFileReader.COMMENT_LINE_CHAR, GENERAL_REPLACER_CHAR );
    }

}
