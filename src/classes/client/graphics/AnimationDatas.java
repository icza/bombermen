
/*
 * Created on September 20, 2004
 */

package classes.client.graphics;

import java.awt.Color;

/**
 * Holds the datas of an animation.<br>
 * The datas of an animation contains a list of images, more precisely handlers of images.
 * All images should (must) have the same dimension.
 *
 * @author Andras Belicza
 */
public class AnimationDatas {

    /** Array of image handlers handling the frames of the animation. */
    public final ImageHandler[] frameHandlers;
    /** Tells how many frames should be displayed during one second.  */
    public final int            framesPerSec;
	/** Background color of the animation (should be used to paint the component around the animation if it doesn't fit it perfectly). */
	public final Color          backgroundColor;

    /**
     * Creates a new AnimationDatas.
     * @param frameHandlers   array of image handlers handling the frames of the animation
     * @param framesPerSec    tells how many frames should be displayed during a second
     * @param backgroundColor background color of the animation
     */
    public AnimationDatas( final ImageHandler[] frameHandlers, final int framesPerSec, final Color backgroundColor ) {
        this.frameHandlers   = frameHandlers;
        this.framesPerSec    = framesPerSec;
		this.backgroundColor = backgroundColor;
    }

}
