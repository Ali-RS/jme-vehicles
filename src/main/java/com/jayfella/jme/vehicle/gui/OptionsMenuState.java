package com.jayfella.jme.vehicle.gui;

import com.simsilica.lemur.Button;

public class OptionsMenuState extends AnimatedMenuState {

    public OptionsMenuState() {

    }

    @Override
    protected Button[] createItems() {
        Button[] buttons = new Button[] {
                new Button("Something"),
                new Button("<< Back")
        };

        buttons[1].addClickCommands(source -> {
            animateOut(() -> {
                getStateManager().attach(new MainMenuState());
                getStateManager().detach(this);
            });
        });

        return buttons;
    }



}
