
/*
 * Created on August 28, 2004
 */

package classes.utils;

import java.awt.Window;
import java.awt.Rectangle;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.File;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 * Provides general utilities.
 *
 * @author Andras Belicza
 */
public class GeneralUtilities {

    /**
     * Repositions a window so it will be in the center of another window.
     * @param window   window to be centered
     * @param toWindow window where to be center the other window
     */
    public static void centerWindowToWindow( final Window window, final Window toWindow ) {
        // Bounds of the valuable part of display
        final Rectangle displayBounds  = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        final Rectangle toWindowBounds = toWindow.getBounds();
        window.setLocation( Math.max( Math.min( toWindowBounds.x + ( toWindowBounds.width  - window.getWidth () ) / 2, displayBounds.x + displayBounds.width  - window.getWidth () ), 0 ),
                            Math.max( Math.min( toWindowBounds.y + ( toWindowBounds.height - window.getHeight() ) / 2, displayBounds.y + displayBounds.height - window.getHeight() ), 0 )  );
    }

    /**
     * Returns the array of names of directories within a directory.
     * @param directoryName the name of directory where to search for subdirectoires within
     * @return the array of names of directories within the directory
     */
    public static String[] getSubdirectoryNames( final String directoryName ) {
        final File[] fileList = new File( directoryName ).listFiles();
		
        if ( fileList == null )
            return new String[ 0 ];

        final List< String > subdirectoryNames = new ArrayList< String >();
        for ( final File file : fileList )
            if ( file.isDirectory() )
                subdirectoryNames.add( file.getName() );
		
        return subdirectoryNames.toArray( new String[ subdirectoryNames.size() ] );
    }
    
    /**
     * Returns the array of file names without extension within a directory.
     * @param directoryName name of directory where to search for files
     * @param extension     extension of files to search for
     * @return the array of file names without extension within the directory
     */
    public static String[] getFileNamesWithoutExtension( final String directoryName, final String extension ) {
        final File[] fileList = new File( directoryName ).listFiles();
		
        if ( fileList == null )
            return new String[ 0 ];
        
        final List< String > fileNamesWithoutExtension = new ArrayList< String >();
        for ( final File file : fileList ) {
            final String fileName = file.getName();
            if ( file.isFile() && fileName.endsWith( extension ) )
                fileNamesWithoutExtension.add( fileName.substring( 0, fileName.length() - extension.length() ) );
        }
		
        return fileNamesWithoutExtension.toArray( new String[ fileNamesWithoutExtension.size() ] );
    }
    
    /**
     * Creates and returns a buffered image which is compatible with the default display.
     * Can be used to create buffered images for high performance.
     * @param width  width of the createable buffered image
     * @param height height of the createable buffered image
     * @param opaque tells whether the createable buffered image should be (is) opaque
     * @return a buffered image with the specified properties whic is compatible with the default display
     */
    public static BufferedImage createDisplayCompatibleBufferedImage( final int width, final int height, final boolean opaque ) {
        if ( opaque )
            return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
                       .createCompatibleImage( width, height );
        else
            return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
                       .createCompatibleImage( width, height, Transparency.TRANSLUCENT );
    }
    
    /**
     * Checks whether 2 rectanges intersect.<br>
     * The 2 rectanges are specified with coordinates of their left top and right bottom points. 
     * @param x11 x coordinate of the left top point of the first rectangle
     * @param y11 y coordinate of the left top point of the first rectangle
     * @param x12 x coordinate of the right bottom point of the first rectangle
     * @param y12 y coordinate of the right bottom point of the first rectangle
     * @param x21 x coordinate of the left top point of the second rectangle
     * @param y21 y coordinate of the left top point of the second  rectangle
     * @param x22 x coordinate of the right bottom point of the second  rectangle
     * @param y22 y coordinate of the right bottom point of the second  rectangle
     * @return true if the specified 2 rectangle intersect; false otherwise
     */
    public static boolean intersect( final int x11, final int y11, final int x12, final int y12, final int x21, final int y21, final int x22, final int y22 ) {
    	return !( x12 < x21 || x11 > x22 || y12 < y21 || y11 > y22 );
    }
    
    /**
     * Performs a weighted random pick.<br>
     * Returns a number between 0 and weights.length chosen randomly based on the weigths table using the specified random object.
     * @param weights weights table to based the decision on
     * @param random  random object to be used for the random decision
     * @return a number between 0 and weights.length chosen randomly based on the weigths table using the specified random object
     */
    public static int pickWeightedRandom( final int[] weights, final Random random ) {
    	int sumWeight = 0;

    	for ( final int weight : weights )
    		sumWeight += weight;
    	
    	int randomWeight = random.nextInt( sumWeight );
    	
    	for ( int i = 0; i < weights.length; i++ ) {
    		randomWeight -= weights[ i ];
    		if ( randomWeight < 0 )
    			return i;
    	}
    	
    	return 0;
    }
    
}
