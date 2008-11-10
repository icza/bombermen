
/*
 * Created on September 4, 2004
 */

package classes.client.graphics;

import javax.swing.JComponent;
import classes.utils.ControlledTimer;
import classes.utils.Timeable;

import java.awt.Graphics;
import java.awt.Image;

/**
 * A component which displays an animation.
 * The component itself is responsible for handling the animation: timing and changing frames and
 * triggering repaintings (by implementing Timeable interface).Timing of frames of animation is done
 * by a controlled timer object.
 *
 * @author Andras Belicza
 */
public class AnimationComponent extends JComponent implements Timeable {

    /** Datas of the animation played on this component.                */
    private final AnimationDatas animationDatas;    
    /** Timer for timing the frames of the animation.                   */
    private ControlledTimer      frameTimer;
    /** Index of the actual frame.                                      */
    private volatile int         actualFrameIndex;

    /**
     * Creates a new AnimationComponent.
     * The view of the component will be the first frame of the animation
     * until animation will be started.
     * @param animationDatas datas of the animation played on this component
     */
    public AnimationComponent( final AnimationDatas animationDatas ) {
        this.animationDatas = animationDatas;

		setDoubleBuffered( false );   // No need to be double buffered, we draw complete images.
        rewindAnimation();
    }
    
    /**
     * Plays the animation.
     * If animation was played previously, it will be continued from the last frame.
     */
    public void playAnimation() {
        if ( animationDatas.frameHandlers.length > 1 ) {   // 1 frame does not need to be animated
            frameTimer = new ControlledTimer( this, animationDatas.framesPerSec );
            frameTimer.start();
        }
    }
    
    /**
     * Stops the animation.
     * The view of the component will be the frame where the animation is stopped.
     */
    public void pauseAnimation() {
        if ( frameTimer != null ) {
            frameTimer.shutDown();
            frameTimer = null;
        }
    }

    /**
     * Rewinds the animation: sets the 0th frame to be actual.
     */
    public void rewindAnimation() {
        actualFrameIndex = 0;
    }
        
    /**
     * Method to be called when it's time to select the next frame.
     */
    public void signalingNextIteration() {
        actualFrameIndex = ++actualFrameIndex % animationDatas.frameHandlers.length;
        // TODO: look after: this should be a call that invokes repaint() later, we should return as soon as possible!
        repaint();
    }
    
    /**
     * Paints the actual view of the component.
     * Draws the actual frame on the component.
     * @param graphics the graphics context in which to paint
     */
    public void paint( final Graphics graphics ) {
		final int          width        = getWidth ();
		final int          height       = getHeight();
		
		final ImageHandler frameHandler = animationDatas.frameHandlers[ actualFrameIndex ];
        final float        xFactor      = (float) width  / frameHandler.getOriginalWidth ();
        final float        yFactor      = (float) height / frameHandler.getOriginalHeight();
        final float        scaleFactor  = Math.min( xFactor, yFactor );
        final int          x            = ( width  - (int) ( frameHandler.getOriginalWidth () * scaleFactor ) ) / 2;
        final int          y            = ( height - (int) ( frameHandler.getOriginalHeight() * scaleFactor ) ) / 2;
		final Image        frame        = frameHandler.getScaledImage( scaleFactor );
		final int		   frameWidth   = frame.getWidth ( null );
		final int		   frameHeight  = frame.getHeight( null );
        
		graphics.drawImage( frame, x, y, null );
		
		graphics.setColor( animationDatas.backgroundColor );
		if ( x > 0 ) {
			graphics.fillRect( 0             , 0, x    , height );
			graphics.fillRect( x + frameWidth, 0, x + 1, height );      // the last column might be out of component, that case it will be clipped
		}
		if ( y > 0 ) { // We examine both condition, could be that frame fits the component perfectly (thats why we don't use else branch...)
			graphics.fillRect( 0, 0              , frameWidth, y     );
			graphics.fillRect( 0, y + frameHeight, frameWidth, y + 1 ); // the last row might be out of component, that case it will be clipped
		}

		if ( frameTimer != null )
            frameTimer.setReadyForNextIteration();
    }

}
