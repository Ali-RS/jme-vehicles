package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jayfella.jme.vehicle.part.Wheel;
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
        List<Button> result = new ArrayList<>(8);

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
        button.addClickCommands(source -> animateOut(()
                -> goTo(new CustomizationMenu())
        ));
        result.add(button);

        return result;
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        MavDemo1.getVehicle().setBurningRubber(false);
        super.onDisable();
    }

    /**
     * Callback invoked whenever this menu becomes both attached and enabled.
     */
    @Override
    protected void onEnable() {
        super.onEnable();
        MavDemo1.getVehicle().setBurningRubber(true);
    }
    // *************************************************************************
    // private methods

    private void setColor(float red, float green, float blue) {
        float alpha = 0.3f;
        ColorRGBA color = new ColorRGBA(red, green, blue, alpha);

        Vehicle vehicle = MavDemo1.getVehicle();
        for (Wheel wheel : vehicle.listWheels()) {
            wheel.setTireSmokeColor(color);
        }
    }
}
