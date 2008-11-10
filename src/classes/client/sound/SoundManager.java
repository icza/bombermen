
/*
 * Created on August 29, 2004
 */

package classes.client.sound;

import classes.utils.GeneralUtilities;
import static classes.Consts.SOUND_DIRECTORY_NAME;

/**
 * Manages the sound resources of the game.<br>
 * This includes getting a list of available sound themes, and loading them.
 *
 * @author Andras Belicza
 */
public class SoundManager {
    
    /**
     * This private SoundManager constructor disables the creation of instances.
     */
    private SoundManager() {
    }
    
    /**
     * Returns array of the names of available sound themes.<br>
     * Returns array the subdirectory names within the SOUND_DIRECTORY_NAME directory.
     * @return array of the names of available sound themes
     */
    public static String[] getAvailableSoundThemes() {
        return GeneralUtilities.getSubdirectoryNames( SOUND_DIRECTORY_NAME );
    }
    
}
