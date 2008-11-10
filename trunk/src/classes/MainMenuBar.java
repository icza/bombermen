
/*
 * Created on July 9, 2004
 */

package classes;

import javax.swing.*;
import java.awt.event.*;
import classes.utils.GeneralStringTokenizer;
import static classes.utils.GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR;

/**
 * This is the main menu bar of the game.
 *
 * @author Andras Belicza
 */
public class MainMenuBar extends JMenuBar implements ActionListener {

    /**
     * This is a menu item descriptor class.
     * @author Andras Belicza
     */
    private static class MID {

        /** Name of the menu item.                                                  */
        public final String  name;
        /** Position of character in the name to be the mnemonic for the menu item. */
        public final int     mnemonicCharPos;
        /** Tells whether this menu item should be a checkbox menu item.            */
        public final boolean hasCheckbox;
        /** Tells whether this menu item is followed by a separator.                */
        public final boolean followedBySeparator;
        
        /**
         * Creates a new MID.
         * @param name name of the menu item
         */
        MID( final String name ) {
            this( name, 0, false );
        }

        /**
         * Creates a new MID.
         * @param name            name of the menu item
         * @param mnemonicCharPos position of character in name to be the mnemonic for the menu item
         */
        MID( final String name, final int mnemonicCharPos ) {
            this( name, mnemonicCharPos, false );
        }

        /**
         * Creates a new MID.
         * @param name            name of the menu item
         * @param mnemonicCharPos position of character in name to be the mnemonic for the menu item
         * @param hasCheckbox     tells whether this menu item should be a checkbox menu item
         */
        MID( final String name, final int mnemonicCharPos, final boolean hasCheckbox ) {
            this( name, mnemonicCharPos, hasCheckbox, false );
        }

        /**
         * Creates a new MID.
         * @param name                name of the menu item
         * @param mnemonicCharPos     position of character in name to be the mnemonic for the menu item
         * @param hasCheckbox         tells whether this menu item should be a checkbox menu item
         * @param followedBySeparator tells whether this menu item is followed by a separator
         */
        MID( final String name, final int mnemonicCharPos, final boolean hasCheckbox, final boolean followedBySeparator ) {
            this.name                = name;
            this.mnemonicCharPos     = mnemonicCharPos;
            this.hasCheckbox         = hasCheckbox;
            this.followedBySeparator = followedBySeparator;
        }

    }
    
    /** The descriptors of menus and menu items of the game (it's hierarchical, the first value is always the menu, the others are the menu items in the menu). */
    private final static MID[][] MENU_ITEM_DESCRIPTORSS = {
        { new MID( "Game" ),
		      new MID( "Create" ), new MID( "Join" ), new MID( "Start current game" ), new MID( "End current game" ), new MID( "Close", 0, false, true ), new MID( "Exit", 1 ) },
        { new MID( "Settings" ),
		      new MID( "Client options" ), new MID( "Server options" ), new MID( "View global server options", 0, false, true ), new MID( "Fullscreen window", 0, true, true ), new MID( "Sound effects", 1, true ) },
        { new MID( "Tools" ),
		      new MID( "Level editor" ) },
        { new MID( "Help" ),
		      new MID( "Keys" ), new MID( "Manual" ), new MID( "Faqs" ), new MID( "Tips" ), new MID( "My host name and ip", 3 ), new MID( "Credits", 0, false, true ), new MID( "About" ) }
    };


    /**
     * The list of menu items.
     * @author Andras Belicza
     */
    protected enum MenuItems {
        /** Create menu item.                     */
        CREATE,
        /** Join menu item.                       */
        JOIN,
        /** Start current game menu item.         */
        START_CURRENT_GAME,
        /** End current game menu item.           */
        END_CURRENT_GAME,
        /** Close menu item.                      */
        CLOSE,
        /** Exit menu item.                       */
        EXIT,
        /** Client options menu item.             */
        CLIENT_OPTIONS,
        /** Server options menu item.             */
        SERVER_OPTIONS,
        /** View global server options menu item. */
        VIEW_GLOBAL_SERVER_OPTIONS,
        /** Fullscreen window menu item.          */
        FULLSCREEN_WINDOW,
        /** sound effects menu item.              */
        SOUND_EFFECTS,
        /** Level editor menu item.               */
        LEVEL_EDITOR,
        /** Keys menu item.                       */
        KEYS,
        /** Manual menu item.                     */
        MANUAL,
        /** Tips menu item.                       */
        TIPS,
        /** Faqs menu item.                       */
        FAQS,
        /** My host name and ip menu item.        */
        MY_HOST_NAME_AND_IP,
        /** Credits menu item.                    */
        CREDITS,
        /** About menu item.                      */
        ABOUT
    }

    /**
     * The states of the game.
     * @author Andras Belicza
     */
    public enum GameStates {
        /** Idle state, no game running.                                                     */
        IDLE,
        /** Player collecting phase, a server has been created, but we're not connected yet. */
        PLAYER_COLLECTING_NOT_CONNECTED,
        /** Player collecting phase, a server has been created and we're connected to it.    */
        PLAYER_COLLECTING_CONNECTED,
        /** Playing state, all the clients have joined, and game started.                    */
        PLAYING
    }


    /** References to the menu items.                                                        */
    private final JMenuItem[]     menuItems = new JMenuItem[ MenuItems.values().length ];
    /** The menu handler which has the appropriate methods to handle menu item actions.      */
    private final MainMenuHandler mainMenuHandler;
    /** Tells whether our server is running (whether we have a Server object created by us). */
    private boolean               ourServerRunning;
    /** State of the (client) game.                                                          */
    private GameStates            gameState;

    /**
     * Creates a new MainMenuBar.
     * @param mainMenuHandler reference to the main menu handler
     */
    MainMenuBar( final MainMenuHandler mainMenuHandler ) {
        this.mainMenuHandler = mainMenuHandler;
        buildGUI();
    }
    
    /**
     * Builds the graphical user interface of the main menubar.
     */
    private void buildGUI() {
        int menuItemCounter = 0;
        
        for ( final MID[] menuItemDescriptors : MENU_ITEM_DESCRIPTORSS ) {
            boolean firstItem = true;
            JMenu   menu      = null;

            for ( final MID menuItemDescriptor : menuItemDescriptors ) {

                JMenuItem menuItem;
                if ( firstItem )
                    menu = (JMenu) ( menuItem = new JMenu( menuItemDescriptor.name ) );
                else {
                    if ( menuItemDescriptor.hasCheckbox )
                        menuItem = new JCheckBoxMenuItem( menuItemDescriptor.name );
                    else
                        menuItem = new JMenuItem( menuItemDescriptor.name );
                    menuItems[ menuItemCounter++ ] = menuItem;
                }

                menuItem.setMnemonic( menuItem.getText().charAt( menuItemDescriptor.mnemonicCharPos ) );
                menuItem.addActionListener( this );

                if ( firstItem )
                    add( menu );
                else {
                    menu.add( menuItem );
                    if ( menuItemDescriptor.followedBySeparator )
                        menu.addSeparator();
                }

                if ( firstItem )
                    firstItem = false;
            }
        }
    }

    /**
     * Handles action events of menu items.
     * @param ae details of the action event
     */
    public void actionPerformed( final ActionEvent ae ) {
        final Object eventSource = ae.getSource();
        for ( final MenuItems menuItem : MenuItems.values() )
            if ( menuItems[ menuItem.ordinal() ] == eventSource ) {
                switch ( menuItem ) {
                    case CREATE                     : mainMenuHandler.createGame();                    break;
                    case JOIN                       : mainMenuHandler.joinAGame();                     break;
                    case START_CURRENT_GAME         : mainMenuHandler.startCurrentGame();              break;
                    case END_CURRENT_GAME           : mainMenuHandler.endCurrentGame();                break;
                    case CLOSE                      : mainMenuHandler.closeGame();                     break;
                    case EXIT                       : mainMenuHandler.exit();                          break;
                    case CLIENT_OPTIONS             : mainMenuHandler.showClientOptionsDialog();       break;
                    case SERVER_OPTIONS             : mainMenuHandler.showServerOptionsDialog();       break;
                    case VIEW_GLOBAL_SERVER_OPTIONS : mainMenuHandler.showGlobalServerOptionsDialog(); break;
                    case FULLSCREEN_WINDOW          : mainMenuHandler.setFullScreenMode( ( (JCheckBoxMenuItem) menuItems[ MenuItems.FULLSCREEN_WINDOW.ordinal() ] ).getState() ); break;
                    case SOUND_EFFECTS              : break;
                    case LEVEL_EDITOR               : break;
                    case MANUAL                     : break;
                    case FAQS                       : break;
                    case MY_HOST_NAME_AND_IP        : break;
                    case CREDITS                    : break;
                    case ABOUT                      : break;
                }
                break;
            }
    }

    /**
     * Sets the state of the game which means to enable and disable menu items
     * what are available/unavailable in the current state of the game.
     * @param gameState the current game state
     */
    public void setGameState( final GameStates gameState ) {
        this.gameState = gameState;
        menuItems[ MenuItems.CREATE.ordinal() ].setEnabled( this.gameState == GameStates.IDLE );
        menuItems[ MenuItems.JOIN  .ordinal() ].setEnabled( this.gameState == GameStates.IDLE );

        menuItems[ MenuItems.START_CURRENT_GAME.ordinal() ].setEnabled( this.gameState == GameStates.PLAYER_COLLECTING_CONNECTED && ourServerRunning );
        menuItems[ MenuItems.END_CURRENT_GAME  .ordinal() ].setEnabled( this.gameState == GameStates.PLAYING && ourServerRunning );

        menuItems[ MenuItems.CLOSE.ordinal() ].setEnabled( this.gameState != GameStates.IDLE );

        menuItems[ MenuItems.VIEW_GLOBAL_SERVER_OPTIONS.ordinal() ].setEnabled( this.gameState != GameStates.IDLE );
    }

    /**
     * Returns the state of the game.
     * @return the state of the game
     */
    public GameStates getGameState() {
        return gameState;
    }

    /**
     * Sets the server running state (ourServerRunning attribute).
     * @param ourServerRunning the new running state of our server
     */
    public void setOurServerRunning( final boolean ourServerRunning ) {
        this.ourServerRunning = ourServerRunning;
    }
    
    /**
     * Packs the states of the checkbox menu items (the states of checkbox menu items) to a String.
     * @return a String object holding the states of the checkbox menu items
     */
    public String packMenuStates() {
        final StringBuilder buffer = new StringBuilder();

        buffer.append( ( (JCheckBoxMenuItem) menuItems[ MenuItems.FULLSCREEN_WINDOW.ordinal() ] ).getState() );
        buffer.append( GENERAL_SEPARATOR_CHAR );
        buffer.append( ( (JCheckBoxMenuItem) menuItems[ MenuItems.SOUND_EFFECTS    .ordinal() ] ).getState() );
        buffer.append( GENERAL_SEPARATOR_CHAR );
        
        return buffer.toString();
    }

    /**
     * Sets the states of checkbox menu items to the given ones.
     * @param menuStates the packed menu states to be set
     */
    public void setMenuStates( final String menuStates ) {
        final GeneralStringTokenizer optionsTokenizer = new GeneralStringTokenizer( menuStates );
        ( (JCheckBoxMenuItem) menuItems[ MenuItems.FULLSCREEN_WINDOW.ordinal() ] ).setState( optionsTokenizer.nextBooleanToken() );
        ( (JCheckBoxMenuItem) menuItems[ MenuItems.SOUND_EFFECTS    .ordinal() ] ).setState( optionsTokenizer.nextBooleanToken() );

    }
    
    /**
     * Restores the default the states of checkbox menu items.
     */
    public void restoreDefaultMenuStates() {
        setMenuStates( "true" + GENERAL_SEPARATOR_CHAR + "true" + GENERAL_SEPARATOR_CHAR );
    }

    /**
     * Returns the state of the fullscreen window checkbox menu item.
     * @return the state of the fullscreen window checkbox menu item
     */
    public boolean getFullScreenWindowMenuItemState() {
        return ( (JCheckBoxMenuItem) menuItems[ MenuItems.FULLSCREEN_WINDOW.ordinal() ] ).getState();
    }

    /**
     * Returns the state of the sound effects checkbox menu item.
     * @return the state of the sound effects checkbox menu item
     */
    public boolean getSoundEffectMenuItemState() {
        return ( (JCheckBoxMenuItem) menuItems[ MenuItems.SOUND_EFFECTS.ordinal() ] ).getState();
    }

}
