
/*
 * Created on September 1, 2004
 */

package classes.utils;

import java.awt.Color;
import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;

/**
 * An icon to show a color.<br>
 * Its view is a filled rectangle in the color of the icon.
 *
 * @author Andras Belicza
 */
public class ColorIcon implements Icon {

    /** The width of the color icons.  */
    private static final int WIDTH  = 18;
    /** The height of the color icons. */
    private static final int HEIGHT = WIDTH;

    /** The color of the icon. */
    private final Color color;

    /**
     * Creates a new ColorIcon.
     * @param color the color of the icon
     */
    public ColorIcon( final Color color ) {
        this.color = color;
    }

    /**
     * Returns the width of the icon.
     * @return the width of the icon
     */
    public int getIconWidth() {
        return WIDTH;
    }
    
    /**
     * Returns the height of the icon.
     * @return the height of the icon
     */
    public int getIconHeight() {
        return HEIGHT;
    }
    
    /**
     * Paints the icon to the component at the x;y position using the graphics context.<br>
     * A filled rectangle will be draw at the x;y position with the width and height returned by
     * getIconWidth() and getIconHeight() and using the color of icon specified at the constructor.
     *
     * @param component what this icon will be painted on
     * @param graphics  the graphics context in which to paint
     * @param x         x coordinate of position where the icon must be painted
     * @param y         y coordinate of position where the icon must be painted
     */
    public void paintIcon( final Component component, final Graphics graphics, final int x, final int y ) {
        graphics.setColor( color );
        graphics.fillRect( x, y, WIDTH, HEIGHT );
    }
    
}
