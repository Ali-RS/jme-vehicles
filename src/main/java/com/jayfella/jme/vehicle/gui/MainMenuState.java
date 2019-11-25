package com.jayfella.jme.vehicle.gui;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;

public class MainMenuState extends AnimatedMenuState {

    public MainMenuState() {

    }

    @Override
    protected Button[] createItems() {
        Button[] buttons = new Button[] {
                new Button("Select Vehicle"),
                new Button("Options"),
                new Button("Exit Game")
        };

        // select vehicle button
        buttons[0].addClickCommands(source -> {
            animateOut(() -> {

                BulletAppState bulletAppState = getState(BulletAppState.class);
                Node playground = (Node) ((SimpleApplication)getApplication()).getRootNode().getChild("playground");

                getStateManager().attach(new CarSelectorMenuState(playground, bulletAppState.getPhysicsSpace()));

                getStateManager().detach(this);
            });
        });

        // options button
        buttons[1].addClickCommands(source -> {
            animateOut(() -> {
                getStateManager().attach(new OptionsMenuState());
                getStateManager().detach(this);
            });

        });

        // exit button
        buttons[2].addClickCommands(source -> {
            animateOut(() -> getApplication().stop());
        });

        return buttons;
    }


}
