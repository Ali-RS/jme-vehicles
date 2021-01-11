package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.examples.skies.AnimatedNightSky;
import com.jayfella.jme.vehicle.examples.skies.QuarrySky;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to choose among the available skies.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class SkyMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SkyMenu.class.getName());
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected List<Button> createItems() {
        List<Button> result = new ArrayList<>(3);

        Button button = new Button("Animated Night");
        button.addClickCommands(source -> setSky(new AnimatedNightSky()));
        result.add(button);

        button = new Button("Quarry");
        button.addClickCommands(source -> setSky(new QuarrySky()));
        result.add(button);

        button = new Button("<< Back");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new CustomizationMenu())
        ));
        result.add(button);

        return result;
    }
    // *************************************************************************
    // private methods

    private void setSky(Sky sky) {
        sky.load();
        Main.getApplication().setSky(sky);
    }
}
