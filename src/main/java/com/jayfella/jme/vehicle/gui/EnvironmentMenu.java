package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Environment;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Playground;
import com.jayfella.jme.vehicle.Racetrack;
import com.jme3.app.state.AppStateManager;
import com.simsilica.lemur.Button;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to choose among the available environments.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class EnvironmentMenu extends AnimatedMenu {
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
        AppStateManager stateManager = getStateManager();

        Button playgroundButton = new Button("Playground");
        playgroundButton.addClickCommands(source
                -> setEnvironment(new Playground()));

        Button racetrackButton = new Button("Racetrack");
        racetrackButton.addClickCommands(source
                -> setEnvironment(new Racetrack()));

        Button backButton = new Button("<< Back");
        backButton.addClickCommands(source -> {
            stateManager.attach(new MainMenu());
            stateManager.detach(this);
        });

        Button[] result = new Button[]{
            playgroundButton,
            racetrackButton,
            backButton
        };
        return result;
    }
    // *************************************************************************
    // private methods

    private void setEnvironment(Environment environment) {
        environment.load();
        Main.getApplication().setEnvironment(environment);
    }
}
