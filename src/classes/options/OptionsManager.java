
/*
 * Created on July 3, 2004
 */

package classes.options;

import java.awt.event.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.util.Vector;

import classes.options.model.Options;
import classes.utils.GeneralUtilities;

/**
 * This class manages the options belonging together.
 * The manageable component options object must be passed to the constructor.
 *
 * @param <OptionsType> tells what kind of options is managed by this manager 
 * @author Andras Belicza
 */
public class OptionsManager< OptionsType extends Options< OptionsType > > implements ActionListener {

    /** Label of the Ok button.               */
    private static final String OK_BUTTON_LABEL               = "Ok";
    /** Label of the Restore defaults button. */
    private static final String RESTORE_DEFAULTS_BUTTON_LABEL = "Restore defaults";
    /** Label of the Cancel button.           */
    private static final String CANCEL_BUTTON_LABEL           = "Cancel";

    /** Reference to the component options to be managed.       */
    private final ComponentOptions< OptionsType > componentOptions;
    /** Title of the options dialog.                            */
    private final String                          optionsDialogTitle;
    /** Reference to the main frame.                            */
    private final JFrame                          mainFrame;
    /** The panel containing buttons to control option dialogs. */
    private final JPanel                          buttonsPanel = new JPanel();
    /** The options dialog.                                     */
    private JDialog                               optionsDialog;
    /** Tells whether we just want to view the options but not to modify. */
    private final boolean                         viewOnly;

    /** Vector of registered options change listeners.          */
    private final Vector< OptionsChangeListener< OptionsType > > optionsChangeListeners = new Vector< OptionsChangeListener< OptionsType > >();

    
    /**
     * Creates a new OptionsManager.<br>
     * The new options manager will be able to change the options.
     * @param componentOptions component options to be managed
     * @param optionsDialogTitle title of the options dialog
     * @param mainFrame reference to the main frame
     */
    public OptionsManager( final ComponentOptions< OptionsType > componentOptions, final String optionsDialogTitle, final JFrame mainFrame ) {
        this( componentOptions, optionsDialogTitle, mainFrame, false );
    }

    /**
     * Creates a new OptionsManager.
     * @param componentOptions   component options to be managed
     * @param optionsDialogTitle title of the options dialog
     * @param mainFrame          reference to the main frame
     * @param viewOnly           tells whether we just want to view the options but not to modify
     */
    public OptionsManager( final ComponentOptions< OptionsType > componentOptions, final String optionsDialogTitle, final JFrame mainFrame, final boolean viewOnly ) {
        this.componentOptions   = componentOptions;
        this.optionsDialogTitle = optionsDialogTitle;
        this.mainFrame          = mainFrame;
        this.viewOnly           = viewOnly;

        final String[] buttonLabels             = { OK_BUTTON_LABEL, RESTORE_DEFAULTS_BUTTON_LABEL, CANCEL_BUTTON_LABEL };
        final String[] buttonLabelsWhenViewOnly = { OK_BUTTON_LABEL };
        for ( final String buttonLabel : this.viewOnly ? buttonLabelsWhenViewOnly : buttonLabels ) {
            final JButton button = new JButton( buttonLabel );
            button.setMnemonic( button.getText().charAt( 0 ) );
            button.addActionListener( this );
            buttonsPanel.add( button );
        }
    }

    /**
     * Returns the managed options object.
     * @return the managed options object
     */
    public OptionsType getOptions() {
        return componentOptions.getOptions();
    }
    
    /**
     * Sets the managed options object.
     * @param options the manageable options object
     */
    public void setOptions( final OptionsType options ) {
        componentOptions.setOptions( options );
    }
    
    /**
     * Shows the options dialog where the options will be displayed and become modifiable.
     */
    public void showOptionsDialog() {
        componentOptions.synchronizeComponentsToOptions();
        optionsDialog = new JDialog( mainFrame, optionsDialogTitle, true );
        optionsDialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        optionsDialog.getContentPane().add( new JScrollPane( componentOptions.getOptionsTabbedPane() ), BorderLayout.CENTER );
        optionsDialog.getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
        optionsDialog.pack();
        GeneralUtilities.centerWindowToWindow( optionsDialog, mainFrame );
        optionsDialog.setVisible( true );
    }

    /**
     * Handles the action events of the buttons of the options dialog.
     * @param ae the details of the action event
     */
    public void actionPerformed( final ActionEvent ae ) {
        if ( viewOnly ) {
            optionsDialog.dispose();
            return;
        }

        final String actionCommand = ae.getActionCommand();

        if ( actionCommand.equals( OK_BUTTON_LABEL ) ) {
            final OptionsType oldOptions = getOptions();
            componentOptions.setOptions( componentOptions.getOptionsFromComponents() );
            final OptionsType newOptions = getOptions();
            
            for ( final OptionsChangeListener< OptionsType > optionsChangeListener : optionsChangeListeners )
                optionsChangeListener.optionsChanged( oldOptions, newOptions );
            
            optionsDialog.dispose();
        }

        else if ( actionCommand.equals( RESTORE_DEFAULTS_BUTTON_LABEL ) ) {
            componentOptions.restoreDefaultValuesToComponents();

        } else if ( actionCommand.equals( CANCEL_BUTTON_LABEL ) ) {
            optionsDialog.dispose();
        }
    }

    /**
     * Registers an options change listener.
     * @param optionsChangeListener options change listener to be registered
     */
    public void registerOptionsChangeListener( final OptionsChangeListener< OptionsType > optionsChangeListener ) {
        optionsChangeListeners.add( optionsChangeListener );
    }
    
    /**
     * Unregisters an options change listener.
     * @param optionsChangeListener options change listener to be unregistered
     */
    public void unregisterOptionsChangeListener( final OptionsChangeListener< OptionsType > optionsChangeListener ) {
        optionsChangeListeners.remove( optionsChangeListener );
    }
    
}
