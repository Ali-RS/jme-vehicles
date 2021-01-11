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
        List<Button> result = new ArrayList<>(5);

        Button button = new Button("Engine Sound");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new EngineSoundMenu())
        ));
        result.add(button);

        button = new Button("Sky");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new SkyMenu())
        ));
        result.add(button);

        button = new Button("Tire Smoke Color");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new TireSmokeColorMenu())
        ));
        result.add(button);

        button = new Button("Wheels");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new WheelMenu())
        ));
        result.add(button);

        button = new Button("<< Back");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new MainMenu())
        ));
        result.add(button);

        return result;
    }
}
