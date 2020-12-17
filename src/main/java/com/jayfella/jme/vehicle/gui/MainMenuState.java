package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Main;
import com.jme3.app.state.AppStateManager;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;

public class MainMenuState extends AnimatedMenuState {

    public MainMenuState() {

    }

    @Override
    protected Button[] createItems() {
        Button[] buttons = new Button[]{
            new Button("Select Environment"),
            new Button("Select Vehicle"),
            new Button("Exit Game")
        };

        // Select Environment button
        buttons[0].addClickCommands(source -> {
            animateOut(() -> {
                AppStateManager stateManager = getStateManager();
                stateManager.attach(new EnvironmentMenu());
                stateManager.detach(this);
            });
        });

        // Select Vehicle button
        buttons[1].addClickCommands(source -> {
            animateOut(() -> {
                AppStateManager stateManager = getStateManager();
                Node envNode = Main.getEnvironment().getCgm();
                stateManager.attach(new CarSelectorMenuState(envNode));
                stateManager.detach(this);
            });
        });

        // exit button
        buttons[2].addClickCommands(source -> {
            animateOut(() -> getApplication().stop());
        });

        return buttons;
    }

}
