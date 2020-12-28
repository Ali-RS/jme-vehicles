package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Environment;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.examples.environments.Playground;
import com.jayfella.jme.vehicle.examples.environments.Racetrack;
import com.jme3.app.state.AppStateManager;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
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
    protected List<Button> createItems() {
        AppStateManager stateManager = getStateManager();
        List<Button> result = new ArrayList<>(3);

        Button button = new Button("Playground");
        button.addClickCommands(source -> setEnvironment(new Playground()));
        result.add(button);

        button = new Button("Racetrack");
        button.addClickCommands(source -> setEnvironment(new Racetrack()));
        result.add(button);

        button = new Button("<< Back");
        button.addClickCommands(source -> {
            stateManager.attach(new MainMenu());
            stateManager.detach(this);
        });
        result.add(button);

        return result;
    }
    // *************************************************************************
    // private methods

    private void setEnvironment(Environment environment) {
        environment.load();
        Main.getApplication().setEnvironment(environment);
    }
}
