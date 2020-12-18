package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Playground;
import com.jayfella.jme.vehicle.Racetrack;
import com.jme3.app.state.AppStateManager;
import com.simsilica.lemur.Button;
import java.util.logging.Logger;

/**
 * An animated menu to choose among the available environments.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class EnvironmentMenu extends AnimatedMenuState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(EnvironmentMenu.class.getName());
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected Button[] createItems() {
        Main application = Main.getApplication();
        AppStateManager stateManager = getStateManager();

        Button playgroundButton = new Button("Playground");
        playgroundButton.addClickCommands(source
                -> application.setEnvironment(new Playground()));

        Button racetrackButton = new Button("Racetrack");
        racetrackButton.addClickCommands(source
                -> application.setEnvironment(new Racetrack()));

        Button backButton = new Button("<< Back");
        backButton.addClickCommands(source -> {
            stateManager.attach(new MainMenuState());
            stateManager.detach(this);
        });

        Button[] result = new Button[]{
            playgroundButton,
            racetrackButton,
            backButton
        };
        return result;
    }
}
