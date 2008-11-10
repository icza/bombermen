
/*
 * Created on October 5, 2004
 */

package classes;

import classes.client.graphics.AnimationComponent;
import classes.client.graphics.AnimationDatas;
import classes.client.graphics.GraphicsManager;

/**
 * A main component handler where the main comonent is an animation component.
 * The class is abstract
 *
 * @author Andras Belicza
 */
public abstract class AbstractAnimationMainComponentHandler implements MainComponentHandler {

    /** Reference to the main frame.                     */
    private final MainFrame    mainFrame;
    /** The animation component being the main component. */
    private AnimationComponent animationComponent;

    /**
     * "Creates" a new AbstractAnimationMainComponentHandler.
     * @param mainFrame reference to the main frame
     */
    public AbstractAnimationMainComponentHandler( final MainFrame mainFrame ) {
        this.mainFrame = mainFrame;
    }

    /**
     * Called when reinitiation of main component is needed.
     */
    public void reinitMainComponent() {
        if ( animationComponent == null ) {
            createNewAnimationComponent();
            if ( animationComponent == null )
                return;
        }
        else {
            mainFrame.setMainComponent( animationComponent );
            animationComponent.rewindAnimation();
        }
        
        animationComponent.playAnimation();
    }

    /**
     * Returns the new animation datas for the animation component being the main component.<br>
     * Called by the graphicalThemeChanged() method to gain the new animation datas.<br>
     * Implementation simply should be: return GraphicsManager.getCurrentManager().getXxxAnimationDatas();
     * @return the new animation datas
     */
    protected abstract AnimationDatas getNewAnimationDatas();

    /**
     * Called when a new graphical theme has been loaded.
     */
    public void graphicalThemeChanged() {
        createNewAnimationComponent();
        if ( animationComponent == null )
            return;
        animationComponent.playAnimation();
    }
    
    /**
     * Creates a new animation component, if a curren graphical manager exists, ands sets it to the main frame.
     */
    private void createNewAnimationComponent() {
        if ( GraphicsManager.getCurrentManager() == null )
            return;
        animationComponent = new AnimationComponent( getNewAnimationDatas() );
        mainFrame.setMainComponent( animationComponent );
    }
    
    /**
     * Called when handler of main component is being replaced, so main component needed to be released.
     */
    public void releaseMainComponent() {
        if ( animationComponent == null )
            return;
        animationComponent.pauseAnimation();
    }

}
