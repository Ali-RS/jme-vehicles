package com.jayfella.jme.vehicle.gui.menu;

import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to access the customization menus.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CustomizationMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(CustomizationMenu.class.getName());
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected List<Button> createItems() {
        List<Button> result = new ArrayList<>(6);

        addButton(result, "Engine Sound",
                source -> animateOut(() -> goTo(new EngineSoundMenu())));
        addButton(result, "Sky",
                source -> animateOut(() -> goTo(new SkyMenu())));
        addButton(result, "Speedometer",
                source -> animateOut(() -> goTo(new SpeedometerMenu())));
        addButton(result, "Tire Smoke Color",
                source -> animateOut(() -> goTo(new TireSmokeColorMenu())));
        addButton(result, "Wheels",
                source -> animateOut(() -> goTo(new WheelMenu())));
        addButton(result, "<< Back",
                source -> animateOut(() -> goTo(new MainMenu())));

        return result;
    }
}
