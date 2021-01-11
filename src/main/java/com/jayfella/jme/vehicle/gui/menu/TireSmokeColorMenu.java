package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to select a color for tire smoke.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class TireSmokeColorMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TireSmokeColorMenu.class.getName());
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
        List<Button> result = new ArrayList<>(6);

        Button button = new Button("Black");
        button.addClickCommands(source -> setColor(0f, 0f, 0f));
        result.add(button);

        button = new Button("Blue");
        button.addClickCommands(source -> setColor(0f, 0f, 1f));
        result.add(button);

        button = new Button("Gray");
        button.addClickCommands(source -> setColor(0.6f, 0.6f, 0.6f));
        result.add(button);

        button = new Button("Green");
        button.addClickCommands(source -> setColor(0f, 1f, 0f));
        result.add(button);

        button = new Button("Red");
        button.addClickCommands(source -> setColor(1f, 0f, 0f));
        result.add(button);

        button = new Button("White");
        button.addClickCommands(source -> setColor(1f, 1f, 1f));
        result.add(button);

        button = new Button("Yellow");
        button.addClickCommands(source -> setColor(1f, 1f, 0f));
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

    private void setColor(float red, float green, float blue) {
        float alpha = 0.3f;
        ColorRGBA color = new ColorRGBA(red, green, blue, alpha);

        Car vehicle = (Car) Main.getVehicle();
        int numWheels = vehicle.countWheels();
        for (int i = 0; i < numWheels; ++i) {
            vehicle.getWheel(i).setTireSmokeColor(color);
        }
    }
}
