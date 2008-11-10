
/*
 * Created on September 5, 2004
 */

package classes.utils;

import java.io.*;

/**
 * A text file reader where datas are stored.<br>
 *
 * @author Andras Belicza
 */
public class DataTextFileReader {

    /** Starter character of comment lines (lines starting with this are considered as comment lines). */
    public static final char   COMMENT_LINE_CHAR = '#';
    /** String starter of comment lines (contains only the comment line character).                    */
    public static final String COMMENT_LINE_HEAD = "" + COMMENT_LINE_CHAR;

    /** The name of data text file to be read.                  */
    private final String         fileName;
    /** Buffered reader object used to read the data text file. */
    private final BufferedReader fileReader;
    /** Number of lines read from the data text file.           */
    private int                  linesRead = 0;

    /**
     * Creates a new DataTextFileReader.
     * @param fileName name of data text file to be read
     * @throws FileNotFoundException if the given file does not exists
     */
    public DataTextFileReader( final String fileName ) throws FileNotFoundException {
        this.fileName = fileName;
        fileReader = new BufferedReader( new FileReader( this.fileName ) );
    }

    /**
     * Returns the name of data text file we're reading from.
     * @return the name of data text file we're reading from
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Reads and returns the next data line skipping comment lines preceding it.
     * @return the next data line, or null if the end of file has been reached of IOException has been thrown
     */
    public String readNextDataLine() {
        String line;
        do {
            line = readNextLine();
            if ( line == null )
                break;
        } while ( line.startsWith( COMMENT_LINE_HEAD ) );
        return line;
    }

    /**
     * Reads and returns the next line in the data text file.
     * @return the next line of the data text file
     */
    private String readNextLine() {
        String line = null;

        try {
            line = fileReader.readLine();
        }
        catch ( final IOException ie ) {
        }

        if ( line != null )
            linesRead++;
        return line;
    }
    
    /**
     * Returns the number of lines read from the data text file.
     * Can be used for indicating line number where error was found...
     * @return the number of lines read from the data text file
     */
    public int getLinesRead() {
        return linesRead;
    }
    
    /**
     * Closes this data text file reader.
     */
    public void close() {
        try {
            fileReader.close();
        }
        catch ( final IOException ie ) {
        }
    }

}
