
/*
 * Created on August 29, 2004
 */

package classes.client.graphics;

import classes.client.gamecore.Consts.FireShapes;
import classes.client.gamecore.Consts.BombTypes;
import classes.client.gamecore.Consts.Activities;
import classes.client.gamecore.Consts.Directions;
import classes.utils.GeneralUtilities;
import static classes.Consts.GRAPHICS_DIRECTORY_NAME;
import classes.utils.DataTextFileReader;
import java.io.FileNotFoundException;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.StringTokenizer;

/**
 * Manages the graphic resources of the game.<br>
 * Provides a list of available graphic themes, and can load them.
 *
 * @author Andras Belicza
 */
public class GraphicsManager {

    /** Name of the theme property file.                       */
    private static final String THEME_PROPERTY_FILE_NAME    = "ThemeProperties.txt";
    /** Name of the window icon image file.                    */
    private static final String WINDOW_ICON_IMAGE_FILE_NAME = "WindowIcon.png";
    /** Name of the title animation file.                      */
    private static final String TITLE_ANIMATION_FILE_NAME   = "TitleAnimation.png";
    /** Name of the waiting animation file.                    */
    private static final String WAITING_ANIMATION_FILE_NAME = "WaitingAnimation.png";
    /** Name of the file containing the wall images.           */
    private static final String WALL_IMAGES_FILE_NAME       = "Walls.png";
    /** Name of the file containing the item images.           */
    private static final String ITEM_IMAGES_FILE_NAME       = "Items.png";
    /** Name of the file containing the phases of a bomberman. */
    private static final String BOMBERMAN_PHASES_FILE_NAME  = "Bomberman.png";
    /** Name of the file containing the phases of a bomb.      */
    private static final String BOMB_PHASES_FILE_NAME       = "Bomb.png";
    /** Name of the file containing the phases of a fire.      */
    private static final String FIRE_PHASES_FILE_NAME       = "Fire.png";
    /** Name of the file containing the phases of the burning. */
    private static final String BURNING_PHASES_FILE_NAME    = "Burning.png";

    
    /** Reference to the current graphics manager (the manager that has been created the most lately). */
    private static GraphicsManager currentManager;

    /** The window icon image.               */
    private Image          windowIconImage;
    /** Datas of the title animation.        */
    private AnimationDatas titleAnimationDatas;
    /** Datas of the waiting animation.      */
    private AnimationDatas waitingAnimationDatas;
	/** Handlers of the images of the walls. */
	private ImageHandler[] wallImageHandlers;
	/** Handlers of the images of the items. */
	private ImageHandler[] itemImageHandlers;
	
	/**
	 * Image handlers of the bomberman phases.
	 * 3 dimensional:
	 *    1st: activity  (classes.client.gamecore.Consts.Activites)
	 *    2nd: direction (classes.client.gamecore.Consts.Directions)
	 *    3rd: phaseIndex
	 */
	private ImageHandler[][][] bombermanPhaseHandlers;

	/**
	 * Image handlers of the bomb phases.
	 * 2 dimensional:
	 *    1st: type (classes.client.gamecore.Consts.BombTypes)
	 *    2nd: phaseIndex
	 */
	private ImageHandler[][]   bombPhaseHandlers;

	/**
	 * Image handlers of the fire phases.
	 * 2 dimensional:
	 *    1st: shape (classes.client.gamecore.Consts.FireShapes)
	 *    2nd: phaseIndex
	 */
	private ImageHandler[][]   firePhaseHandlers;
	
	/** Handlers of the images of the items. */
	private ImageHandler[]     burningPhaseHandlers;

	
	
	
    /**
     * This private GraphicsManager constructor disables the creation of instances.
     */
    private GraphicsManager() {
    }
    
    /**
     * Returns the current graphics manager.
     * @return the current graphics manager
     */
    public static final GraphicsManager getCurrentManager() {
        return currentManager;
    }

    /**
     * Returns array of the names of available graphical themes.<br>
     * Returns array the subdirectory names within the GRAPHICS_DIRECTORY_NAME directory.
     * @return array of the names of available graphical themes
     */
    public static String[] getAvailableGraphicalThemes() {
        return GeneralUtilities.getSubdirectoryNames( GRAPHICS_DIRECTORY_NAME );
    }
    
    /**
     * Loads a graphical theme and makes its graphical manager current and returns it.
     * @param theme name of graphical theme to be loaded
     * @return the graphics manager object managing the loaded theme
     * @throws CorruptGraphicalThemeException if there are missing or corrupt graphics resources
     */
    public static GraphicsManager loadGraphicalTheme( final String theme ) throws CorruptGraphicalThemeException {
        final GraphicsManager graphicsManager           = new GraphicsManager();
        final String          themeDirectoryName        = GRAPHICS_DIRECTORY_NAME + theme + "/";
        final String          themePropertyFileFullName = themeDirectoryName + THEME_PROPERTY_FILE_NAME;
        DataTextFileReader    themePropertyFileReader   = null;
        
        StringTokenizer lineTokenizer;
        
        try {
            themePropertyFileReader = new DataTextFileReader( themePropertyFileFullName );
        }
        catch ( final FileNotFoundException fe ) {
            throw new CorruptGraphicalThemeException( "Can't find theme property file: " + themePropertyFileFullName );
        }

        // Loading the window icon image
        graphicsManager.windowIconImage = new ImageIcon( themeDirectoryName + WINDOW_ICON_IMAGE_FILE_NAME ).getImage();
        if ( graphicsManager.windowIconImage.getWidth( null ) < 0 || graphicsManager.windowIconImage.getHeight( null ) < 0  )
            throw new CorruptGraphicalThemeException( "Cannot load window icon from file " + themeDirectoryName + WINDOW_ICON_IMAGE_FILE_NAME );


		// Reading properties from the theme properties file
		try {
            int width, height, fps, size;

			// Reading properties of the animations and loading them
	        for ( int i = 0; i < 2; i++ ) {
	            lineTokenizer = nextDataLineStringTokenizer( themePropertyFileReader );
	
	            width  = Integer.parseInt( lineTokenizer.nextToken() );
	            height = Integer.parseInt( lineTokenizer.nextToken() );
	            fps    = Integer.parseInt( lineTokenizer.nextToken() );
	
				switch ( i ) {
	                case 0 : graphicsManager.titleAnimationDatas   = loadAnimationDatas( themeDirectoryName + TITLE_ANIMATION_FILE_NAME  , width, height, fps ); break;
	                case 1 : graphicsManager.waitingAnimationDatas = loadAnimationDatas( themeDirectoryName + WAITING_ANIMATION_FILE_NAME, width, height, fps ); break;
	            }
	
	        }
			
			// Reading properties of images and loading them
            lineTokenizer = nextDataLineStringTokenizer( themePropertyFileReader );
			size = Integer.parseInt( lineTokenizer.nextToken() );
			graphicsManager.wallImageHandlers = loadImages( themeDirectoryName + WALL_IMAGES_FILE_NAME, size, size );
			
			
			lineTokenizer = nextDataLineStringTokenizer( themePropertyFileReader );
			size = Integer.parseInt( lineTokenizer.nextToken() );
			graphicsManager.itemImageHandlers = loadImages( themeDirectoryName + ITEM_IMAGES_FILE_NAME, size, size );
			
			
			lineTokenizer = nextDataLineStringTokenizer( themePropertyFileReader );
			width  = Integer.parseInt( lineTokenizer.nextToken() );
			height = Integer.parseInt( lineTokenizer.nextToken() );
			final int[] activityPhasesCounts = new int[ Activities.values().length ];
			for ( int i = 0; i < activityPhasesCounts.length; i++ )
				activityPhasesCounts[ i ] = Integer.parseInt( lineTokenizer.nextToken() );
			graphicsManager.bombermanPhaseHandlers = loadBombermanPhases( themeDirectoryName + BOMBERMAN_PHASES_FILE_NAME, width, height, activityPhasesCounts );
			
			
			lineTokenizer = nextDataLineStringTokenizer( themePropertyFileReader );
			size  = Integer.parseInt( lineTokenizer.nextToken() );
			final int[] bombTypePhasesCounts = new int[ BombTypes.values().length ];
			for ( int i = 0; i < bombTypePhasesCounts.length; i++ )
				bombTypePhasesCounts[ i ] = Integer.parseInt( lineTokenizer.nextToken() );
			graphicsManager.bombPhaseHandlers = loadPhases( themeDirectoryName + BOMB_PHASES_FILE_NAME, size, size, bombTypePhasesCounts );
			
			lineTokenizer = nextDataLineStringTokenizer( themePropertyFileReader );
			size  = Integer.parseInt( lineTokenizer.nextToken() );
			final int[] fireShapePhasesCounts = new int[ FireShapes.values().length ];
			Arrays.fill( fireShapePhasesCounts, Integer.parseInt( lineTokenizer.nextToken() ) );
			graphicsManager.firePhaseHandlers = loadPhases( themeDirectoryName + FIRE_PHASES_FILE_NAME, size, size, fireShapePhasesCounts );
			
			lineTokenizer = nextDataLineStringTokenizer( themePropertyFileReader );
			size  = Integer.parseInt( lineTokenizer.nextToken() );
			final int[] burningPhasesCounts = new int[] { Integer.parseInt( lineTokenizer.nextToken() ) };
			graphicsManager.burningPhaseHandlers = loadPhases( themeDirectoryName + BURNING_PHASES_FILE_NAME, size, size, burningPhasesCounts )[ 0 ];
			
		}
		catch ( final NoSuchElementException nsee ) {
            throw new CorruptGraphicalThemeException( constructErrorMessage( "Insufficent datas", themePropertyFileReader ) );
		}
		catch ( final NumberFormatException nfe ) {
            throw new CorruptGraphicalThemeException( constructErrorMessage( "Invalid datas", themePropertyFileReader ) );
		}
        
        themePropertyFileReader.close();

        currentManager = graphicsManager;
        return graphicsManager;
    }

	/**
	 * Reads the next data line of the specified theme property file, and returns a string tokenizer for that string.
	 * @param themePropertyFileReader reference to the data text file reader to be used to obtain the next data line
	 * @return a string tokenizer for the read next data line 
	 * @throws CorruptGraphicalThemeException if reading next line returns null (means end of file has been reached or IOException has been occured)
	 */
	private static StringTokenizer nextDataLineStringTokenizer( final DataTextFileReader themePropertyFileReader ) throws CorruptGraphicalThemeException {
        final String propertyLine = themePropertyFileReader.readNextDataLine();
        if ( propertyLine == null )
            throw new CorruptGraphicalThemeException( constructErrorMessage( "Data read error or insufficent data", themePropertyFileReader ) );
        return new StringTokenizer( propertyLine );
	}
	
	/**
     * Creates and returns an error message to initialize CorruptGraphichalThemeException with.
     * @param errorString the error/reason of exception
     * @param fileReader  the data text file reader used to read graphical theme property file
     * @return the constructed error message
     */
    private static String constructErrorMessage( final String errorString, final DataTextFileReader fileReader ) {
        return errorString + " in file: " + fileReader.getFileName() + " in line: " + fileReader.getLinesRead();
    }
    
    /**
     * Loads and returns datas of an animation from the specified file.
     * @param fileName     name of file containing the frames of the animation
     * @param width        width of the frames of the animation
     * @param height       height of the frames of the animation
     * @param framesPerSec frames per sec attribute of the animation to be used when creating AnimationDatas object
     * @throws CorruptGraphicalThemeException if loading animation datas fails
     * @return an AnimationDatas holding the animations of a graphical theme
     */
    private static AnimationDatas loadAnimationDatas( final String fileName, final int width, final int height, final int framesPerSec ) throws CorruptGraphicalThemeException {
        final int[]          backgroundRGB = new int[ 1 ];
		final ImageHandler[] frameHandlers = loadImages( fileName, width, height, backgroundRGB );
		
		return new AnimationDatas( frameHandlers, framesPerSec, new Color( backgroundRGB[ 0 ] ) );
    }

    /**
     * Loads images from a specified file and returns their image handlers.
     * Implementation is calling the other loadImages() method with null value for parameter backgroundRGB
     * The images must arranged in a column.
     * @param fileName     name of file containing the images in a column with a separator line between them
     * @param width        width of the images
     * @param height       height of the images
     * @throws CorruptGraphicalThemeException if loading of images fails
     * @return an array holding the handlers of the loaded images
     */
    private static ImageHandler[] loadImages( final String fileName, final int width, final int height )  throws CorruptGraphicalThemeException {
		return loadImages( fileName, width, height, null );
    }

	/**
     * Loads images from a specified file and returns their image handlers.<br>
     * The images must arranged in a column.
     * @param fileName      name of file containing the images in a column with a separator line between them
     * @param width         width of the images
     * @param height        height of the images
	 * @param backgroundRGB optional parameter, if not null, the background rgb value of the image will be stored to the 0th element of this array 
     * @throws CorruptGraphicalThemeException if loading of images fails
     * @return an array holding the handlers of the loaded images
     */
    private static ImageHandler[] loadImages( final String fileName, final int width, final int height, final int[] backgroundRGB )  throws CorruptGraphicalThemeException {
        final Image image = new ImageIcon( fileName ).getImage();
        if ( image == null )
            throw new CorruptGraphicalThemeException( "Cannot load graphics data from file: " + fileName + "!" );
        if ( image.getWidth( null ) < width )
            throw new CorruptGraphicalThemeException( "Graphics file " + fileName + " does not contain images having width specified by the theme property file!" );
        if ( image.getHeight( null ) < height )
            throw new CorruptGraphicalThemeException( "Graphics file " + fileName + " must contain at least 1 image having height specified by the theme property file!" );
            
        final BufferedImage bufferedImage = GeneralUtilities.createDisplayCompatibleBufferedImage( image.getWidth( null ), image.getHeight( null ), true );
        bufferedImage.createGraphics().drawImage( image, 0, 0, null );
		
		if ( backgroundRGB != null )
			backgroundRGB[ 0 ] = bufferedImage.getRGB( 0, 0 );
        
        final Vector< ImageHandler > imageHandlers = new Vector< ImageHandler >();
        int yPosition = 0;
        while ( yPosition + height <= bufferedImage.getHeight() ) {
			imageHandlers.add( new ImageHandler( bufferedImage.getSubimage( 0, yPosition, width, height ) ) );
            yPosition += height + 1;     // Plus 1 for the separator line
        }
        
        return imageHandlers.toArray( new ImageHandler[ imageHandlers.size() ] );
    }

	
    /**
     * Loads and returns the image handlers of the bomberman phases.
     * @param fileName             name of the file holding the bomberman phases
     * @param width                width of the bomberman phases
     * @param height               height of the bomberman phases
     * @param activityPhasesCounts phase counts of the different activites
     * @return a 3D array holding all the handlers of the phases of the bomerman
     * @throws CorruptGraphicalThemeException if loading of bomberman phases fails
     */
    private static ImageHandler[][][] loadBombermanPhases( final String fileName, final int width, final int height, final int[] activityPhasesCounts ) throws CorruptGraphicalThemeException {
        final int DIRECTIONS_COUNT = Directions.values().length;

        // There is a phase row for every direction in every activity
    	final int[] phasesCounts = new int[ activityPhasesCounts.length * DIRECTIONS_COUNT ];
    	
    	for ( int i = 0; i < activityPhasesCounts.length; i++ )
    		for ( int j = 0; j < DIRECTIONS_COUNT; j++ )
    			phasesCounts[ i * DIRECTIONS_COUNT + j ] = activityPhasesCounts[ i ];
    	
    	final ImageHandler[][] phaseHandlers = loadPhases( fileName, width, height, phasesCounts );
    	final ImageHandler[][][] bombermanPhaseHandlers = new ImageHandler[ activityPhasesCounts.length ][ DIRECTIONS_COUNT ][];

    	for ( int i = 0; i < activityPhasesCounts.length; i++ )
    		for ( int j = 0; j < DIRECTIONS_COUNT; j++ )
    			bombermanPhaseHandlers[ i ][ j ] = phaseHandlers[ i * DIRECTIONS_COUNT + j ];

        return bombermanPhaseHandlers;
    }
    
    /**
     * Loads phases from a file and returns a 2D ImageHandler array.<br>
     * Phases must be arranged in a row. The file can contain multiple phase rows.<br>
     * Phases are non-opaque images, so after loading them, all pixel having white (255-255-255 RGB) color
     * will be set to a completly non-opaque pixel. 
     * @param fileName     name of file containing the phases in multiple rows with a separator line between them
     * @param width        width of the phases
     * @param height       height of the phases
     * @param phasesCounts array of phases counts in the rows
     * @return a 2D ImageHandler array based on the structure of the image containing the phases
     * @throws CorruptGraphicalThemeException if loading of phases fails
     */
    private static ImageHandler[][] loadPhases( final String fileName, final int width, final int height, final int[] phasesCounts )  throws CorruptGraphicalThemeException {
        final Image image = new ImageIcon( fileName ).getImage();
        if ( image == null )
            throw new CorruptGraphicalThemeException( "Cannot load graphics data from file: " + fileName + "!" );
        
        for ( int i = 0; i < phasesCounts.length; i++ )
        	if ( image.getWidth( null ) < width * phasesCounts[ i ] + phasesCounts[ i ] - 1 )   // These are the plus separator lines
            throw new CorruptGraphicalThemeException( "Graphics file " + fileName + " does not contain enough phases having width specified by the theme property file (phase row: " + (i+1) + ")!" );
        
        if ( image.getHeight( null ) < height * phasesCounts.length + phasesCounts.length - 1 ) // ...the separator lines
            throw new CorruptGraphicalThemeException( "Graphics file " + fileName + " does not contain enough phase rows having height specified by the theme property file!" );
            
        final BufferedImage bufferedImage = GeneralUtilities.createDisplayCompatibleBufferedImage( image.getWidth( null ), image.getHeight( null ), false );
        bufferedImage.createGraphics().drawImage( image, 0, 0, null );
		
		// We set the non-opaque pixels
        for ( int y = bufferedImage.getHeight() - 1; y >= 0; y-- )
			for ( int x = bufferedImage.getWidth() - 1; x >= 0; x-- )
				if ( bufferedImage.getRGB( x, y ) == 0xffffffff )  // The white pixels
						bufferedImage.setRGB( x, y, 0x00000000 );
        
        final ImageHandler[][] phaseHandlers = new ImageHandler[ phasesCounts.length ][];
        for ( int i = 0; i < phasesCounts.length; i++ ) {
        	phaseHandlers[ i ] = new ImageHandler[ phasesCounts[ i ] ];
        	for ( int j = 0; j < phaseHandlers[ i ].length; j++ )
        		phaseHandlers[ i ][ j ] = new ImageHandler( bufferedImage.getSubimage( j * ( width + 1 ), i * ( height + 1 ) , width, height ) );
        }
        
        return phaseHandlers;
    }

    
    
	
	
	/**
     * Returns the window icon image.
     * @return the window icon image
     */
    public Image getWindowIconImage() {
        return windowIconImage;
    }

    /**
     * Returns the title animation datas.
     * @return the title animation datas
     */
    public AnimationDatas getTitleAnimationDatas() {
        return titleAnimationDatas;
    }
    
    /**
     * Returns the waiting animation datas.
     * @return the waiting animation datas
     */
    public AnimationDatas getWaitingAnimationDatas() {
        return waitingAnimationDatas;
    }
    
    /**
     * Returns the handlers of the wall images.
     * @return the handlers of the wall images
     */
    public ImageHandler[] getWallImageHandlers() {
        return wallImageHandlers;
    }
    
    /**
     * Returns the handlers of the item images.
     * @return the handlers of the item images
     */
    public ImageHandler[] getItemImageHandlers() {
        return itemImageHandlers;
    }
    
    /**
     * Returns the image handlers of the bomberman phases.
     * @return the image handlers of the bomberman phases
     */
    public ImageHandler[][][] getBombermanPhaseHandlers() {
        return bombermanPhaseHandlers;
    }
    
    /**
     * Returns the image handlers of the bomb phases.
     * @return the image handlers of the bomb phases
     */
    public ImageHandler[][] getBombPhaseHandlers() {
        return bombPhaseHandlers;
    }
    
    /**
     * Returns the image handlers of the fire phases.
     * @return the image handlers of the fire phases
     */
    public ImageHandler[][] getFirePhaseHandlers() {
        return firePhaseHandlers;
    }
    
    /**
     * Returns the image handlers of the burning phases.
     * @return the image handlers of the burning phases
     */
    public ImageHandler[] getBurningPhaseHandlers() {
        return burningPhaseHandlers;
    }
    
}
