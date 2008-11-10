
/*
 * Created on July 6, 2004
 */

package classes.options.model;

/**
 * This class represents class destined for storing options.
 * 3 main operations is interpreted on options:
 * <UL>
 *    <LI>packToString()   : packs the options to a String object
 *    <LI>parseFromString(): parses an option object from a string
 *    <LI>cloneOptions()   : clones the options object
 * </UL>
 * 
 * @param <OptionsType> tells what kind of options will this type represent dynamically 
 * @author Andras Belicza
 */
public abstract class Options< OptionsType extends Options< OptionsType > > {

    /**
     * Packs the options object to a String so it can be transferred or stored.
     * @return a compact string representing this options
     */
    public abstract String packToString();
    
    /**
     * Parses an options object from a string.<br>
     * Dynamic is for this function is implemented by static methods in OptionsType-s,
     * but we need this service here in method cloneOptions(), so we require a dynamic parser
     * whose implementation can be calling the static parser method.
     * @param source the String representing the parsable options
     * @return a new OptinsType created from the source string
     */
    public abstract OptionsType dynamicParseFromString( final String source );

    /**
     * Clones the options object implementing deep clone by using 
     * the packToString() and parseFromString methods.
     * @return a cloned options object
     */
    public OptionsType cloneOptions() {
        return dynamicParseFromString( packToString() );
    }
    
}
