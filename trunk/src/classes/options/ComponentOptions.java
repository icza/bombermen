
/*
 * Created on July 3, 2004
 */

package classes.options;

import javax.swing.JTabbedPane;
import javax.swing.JLabel;

import classes.options.model.Options;

/**
 * Defines methods to make options available for viewing and changing
 * on the screen.<br>
 * Classes implementing this interface should store a reference to the
 * manageable options object.
 * 
 * @param <OptionsType> tells what kind of options we create and handle components for 
 * @author Andras Belicza
 */
public abstract class ComponentOptions< OptionsType extends Options< OptionsType > > {
    
    /** The options to be handled.                          */
    protected OptionsType       options;
    /** Options tabbed pane holding the options components. */
    protected final JTabbedPane optionsTabbedPane = new JTabbedPane( JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT );

    /**
     * Creates a new ComponentOptions.<br>
     * <b>Warning!!<b> Setting options cannot be done by invoking setOptions method, because
     * ServerComponentOptions overrides setOptions method to invoke his own LevelComponentOptions method too,
     * but at the time when this constructor is invoked, that object cannot be existed, NullPointerException would be thrown.
     * Based on the object hierarchy, this anomaly cannot be solved in other ways.<br>
     * @param options options to be handled
     */
    ComponentOptions( final OptionsType options ) {
        this.options = options;
    }
    
    /**
     * Sets the options to be handled.
     * @param options options to be set to be handled
     */
    public void setOptions( final OptionsType options ) {
        this.options = options;
    }

    /**
     * Restores the default values of the options to the option components.
     */
    public abstract void restoreDefaultValuesToComponents();
    
    /**
     * Creates a new options object, stores into that the actual states of the option components,
     * and returns it.
     * @return the Options object holding the values/states of the option components
     */
    public abstract OptionsType getOptionsFromComponents();
    
    /**
     * Stores the values of the option attributes to the appropriate components.
     */
    public abstract void synchronizeComponentsToOptions();
    
    /**
     * Returns the optiosn tabbed pane containing the option components.<br>
     * The returned tabbed pane is for displaying in the appropriate options dialog,
     * where viewing/changing the values of the options can be done.
     * @return a tabbed pane containing the option components
     */
    public JTabbedPane getOptionsTabbedPane() {
        return optionsTabbedPane;
    }

    /**
     * Returns the options object which is being handled by the implementer class.<br>
     * @return the handled options object
     */
    public OptionsType getOptions() {
        return options;
    }

    /**
     * Returns a cloned options object.
     * @return a cloned options object
     */
    public OptionsType getClonedOptions() {
        return options.cloneOptions();
    }

    /**
     * Creates and returns a JLabel object with controlling its enabled state.
     * @param text the text of the creatable JLabel
     * @param enabled tells whether the creatable JLabel object have to be enabled
     * @return a JLabel object with the specified properties
     */
    protected JLabel createLabel( final String text, final boolean enabled ) {
        final JLabel label = new JLabel( text );
        label.setEnabled( enabled );
        return label;
    }
    
}
