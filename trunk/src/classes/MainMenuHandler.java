
/*
 * Created on July 9, 2004
 */

package classes;

/**
 * Defines methods to handle the action events of the different menu items of the main menu.
 *
 * @author Andras Belicza
 */
interface MainMenuHandler {

    /** 
     * To handle create menu item and create a game.
     */
    void createGame();

    /** 
     * To handle join menu item and join to a game.
     */
    void joinAGame();

    /** 
     * To handle start current game menu item and start current game.
     */
    void startCurrentGame();

    /** 
     * To handle end current game menu item and end current game.
     */
    void endCurrentGame();

    /** 
     * To handle close menu item and close the game.
     */
    void closeGame();

    /** 
     * To handle exit menu item and exit from the game.
     */
    void exit();

    /** 
     * To handle client options menu item, shows the client options dialog.
     */
    void showClientOptionsDialog();

    /** 
     * To handle server options menu item, shows the server options dialog.
     */
    void showServerOptionsDialog();

    /** 
     * To handle view global server options menu item, shows the global server options dialog.
     */
    void showGlobalServerOptionsDialog();

    /**
     * To handle Fullscreen window menu item, sets the full screen window status.
     * @param fullScreen true indicates to be in fullscreen mode; false to be in window mode
     */
    void setFullScreenMode( final boolean fullScreen );

        /** sound effects menu item.              */
        /** Level editor menu item.               */
        /** Manual menu item.                     */
        /** Faqs menu item.                       */
        /** Show my host name and ip menu item.   */
        /** Credits menu item.                    */
        /** About menu item.                      */

}
