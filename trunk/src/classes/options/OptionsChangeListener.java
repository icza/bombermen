
/*
 * Created on August 10, 2004
 */

package classes.options;

import classes.options.model.Options;

/**
 * Defines a method to be called when the listened options may have been changed.

 * @param <OptionsType> tells what kind of options is listened by this listener type 
 * @author Andras Belicza
 */
public interface OptionsChangeListener< OptionsType extends Options< OptionsType > > {

    /**
     * Method to be called when options may have been changed.
     * @param oldOptions the old options before the change signed by calling this method
     * @param newOptions the new options are about to become effective
     */
    void optionsChanged( final OptionsType oldOptions, final OptionsType newOptions );
    
}
