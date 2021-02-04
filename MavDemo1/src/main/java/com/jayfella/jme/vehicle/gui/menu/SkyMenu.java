package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.examples.skies.AnimatedDaySky;
import com.jayfella.jme.vehicle.examples.skies.AnimatedNightSky;
import com.jayfella.jme.vehicle.examples.skies.PurpleNebulaSky;
import com.jayfella.jme.vehicle.examples.skies.QuarrySky;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jme3.asset.AssetManager;
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
        List<Button> result = new ArrayList<>(5);

        Button button = new Button("Animated Day");
        button.addClickCommands(source -> setSky(new AnimatedDaySky()));
        result.add(button);

        button = new Button("Animated Night");
        button.addClickCommands(source -> setSky(new AnimatedNightSky()));
        result.add(button);

        button = new Button("Purple Nebula");
        button.addClickCommands(source -> setSky(new PurpleNebulaSky()));
        result.add(button);

        button = new Button("Quarry Day");
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
        MavDemo1 main = MavDemo1.getApplication();
        AssetManager assetManager = main.getAssetManager();
        sky.load(assetManager);

        main.setSky(sky);
    }
}
