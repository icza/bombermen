
/*
 * Created on August 28, 2004
 */

package classes.client.graphics;

import java.awt.Image;

import classes.options.OptionsChangeListener;
import classes.options.Consts.ImageScalingAlgorithms;
import classes.options.model.ClientOptions;

/**
 * Handles an image.<br>
 * Since the space displaying images and the game scene is changable, different sizes of pictures
 * may be required from moment to moment. This class handles image scaling assuring the minimal calculation
 * if the same dimension of picture is required between 2 requests. Also handles the minimal image loss between the
 * several scaling by storing the original image, and making scaled version from it.
 *
 * @author Andras Belicza
 */
public class ImageHandler {

    /** The original (non-scaled) image.                            */
    private final Image            originalImage;
    /** The lastly requested scaled image.                          */
    private Image                  scaledImage;
    /** Scale factor of the lastly produced image.                  */
    private float                  scaleFactor;
    /** Used image scaling algorithm for the lastly produced image. */
    private ImageScalingAlgorithms usedimageScalingAlgorithm;


    /** The image scaling algoritm to use when scaling the image.   */
    private static ImageScalingAlgorithms imageScalingAlgorithm;
    
    /**
     * We create an options change listener to handle options change events, we need the image scaling algorithm setting.
     */
    public static final OptionsChangeListener< ClientOptions > clientOptionsChangeListener = new OptionsChangeListener< ClientOptions > () {
    	
    	public void optionsChanged( final ClientOptions oldOptions, final ClientOptions newOptions ) {
    		if ( newOptions.imageScalingAlgorithm != oldOptions.imageScalingAlgorithm )
    			imageScalingAlgorithm = newOptions.imageScalingAlgorithm;
    	}
    	
    };
    
    /**
     * Sets the image scaling algorithm.
     * (used for initializing imageScalingAlgorithm)
     * @param imageScalingAlgorithm image scaling algorithm to be set
     */
    public static void setImageScalingAlgorithm( final ImageScalingAlgorithms imageScalingAlgorithm ) {
    	ImageHandler.imageScalingAlgorithm = imageScalingAlgorithm;
    }
    
    
    /**
     * Creates a new ImageHandler.
     * @param image the image to be handled
     */
    public ImageHandler( final Image image ) {
        originalImage = image;
        setScaledImage( originalImage, 1.0f, null );
    }
    
    /**
     * Returns a scaled image of the original one.
     * If the scale factor is equal to the one of the last query, the image will not be scaled again,
     * the same image will be returned. Else a new scaled instance will be calculated and returned.
     * @param scaleFactor scale factor or the required image
     * @return a reference to a scaled instance of this image specified by the scaleFactor attribute
     */
    public Image getScaledImage( final float scaleFactor ) {
        if ( this.scaleFactor == scaleFactor && usedimageScalingAlgorithm == imageScalingAlgorithm )
            return scaledImage;
        // Invoking Math.max() because with or height cannot be zero (and that amount is calculated when the divider of JSplitPane is in outside)
        setScaledImage( originalImage.getScaledInstance( Math.max( (int) ( originalImage.getWidth( null ) * scaleFactor ), 1 ), Math.max( (int) ( originalImage.getHeight( null ) * scaleFactor ), 1 ),
        		        imageScalingAlgorithm == ImageScalingAlgorithms.FAST ? Image.SCALE_FAST : Image.SCALE_SMOOTH ),
        		        scaleFactor, imageScalingAlgorithm );
        return scaledImage;
    }

    /**
     * Sets the scaled image and its properties..
     * @param scaledImage                the new scaled image
     * @param scaleFactor                the new scale factor
     * @param usedImageScalingAlgorithms image scaling algorithm used to produce the scaled version
     */
    private void setScaledImage( final Image scaledImage, final float scaleFactor, final ImageScalingAlgorithms usedImageScalingAlgorithms ) {
        this.scaledImage               = scaledImage;
        this.scaleFactor               = scaleFactor;
        this.usedimageScalingAlgorithm = imageScalingAlgorithm;
    }

    /**
     * Returns the width of the original image.
     * @return the width of the original image
     */
    public int getOriginalWidth() {
        return originalImage.getWidth( null );
    }
    
    /**
     * Returns the height of the original image.
     * @return the height of the original image
     */
    public int getOriginalHeight() {
        return originalImage.getHeight( null );
    }
    
}
