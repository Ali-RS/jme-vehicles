package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Main;
import com.jme3.app.state.AppStateManager;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import java.util.logging.Logger;

public class MainMenuState extends AnimatedMenuState {
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

        Button envButton = new Button("Select Environment");
        envButton.addClickCommands(source -> {
            animateOut(() -> {
                stateManager.attach(new EnvironmentMenu());
                stateManager.detach(this);
            });
        });

        Button vehicleButton = new Button("Select Vehicle");
        vehicleButton.addClickCommands(source -> {
            animateOut(() -> {
                Node envNode = Main.getEnvironment().getCgm();
                stateManager.attach(new CarSelectorMenuState(envNode));
                stateManager.detach(this);
            });
        });

        Button exitButton = new Button("Exit Game");
        exitButton.addClickCommands(source -> {
            animateOut(() -> application.stop());
        });

        Button[] result = new Button[]{
            envButton,
            vehicleButton,
            exitButton
        };
        return result;
    }
}
