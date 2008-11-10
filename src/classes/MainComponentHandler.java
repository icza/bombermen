
/*
 * Created on October 4, 2004
 */

package classes;

/**
 * Defines services needed to be implemented for an object who handles the main component.
 *
 * @author Andras Belicza
 */
public interface MainComponentHandler {

    /**
     * Called when reinitiation of main component is needed.
     */
    void reinitMainComponent();

    /**
     * Called when a new graphical theme has been loaded.
     */
    void graphicalThemeChanged();
    
    /**
     * Called when handler of main component is being replaced.
     */
    void releaseMainComponent();

}
