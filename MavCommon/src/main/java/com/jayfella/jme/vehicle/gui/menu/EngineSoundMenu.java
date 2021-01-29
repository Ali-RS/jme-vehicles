package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Sound;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound1;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound2;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound4;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound5;
import com.jayfella.jme.vehicle.gui.AudioHud;
import com.jayfella.jme.vehicle.lemurdemo.Main;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to choose among the available engine sounds.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class EngineSoundMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(EngineSoundMenu.class.getName());
    // *************************************************************************
    // fields

    /*
     * selected Sound, or null for silence
     */
    private Sound selectedSound;
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

        Button button = new Button("Engine-1");
        button.addClickCommands(source -> setSound(new EngineSound1()));
        result.add(button);

        button = new Button("Engine-2");
        button.addClickCommands(source -> setSound(new EngineSound2()));
        result.add(button);

        button = new Button("Engine-4");
        button.addClickCommands(source -> setSound(new EngineSound4()));
        result.add(button);

        button = new Button("Engine-5");
        button.addClickCommands(source -> setSound(new EngineSound5()));
        result.add(button);

        button = new Button("Silence");
        button.addClickCommands(source -> setSound(null));
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
        if (selectedSound != null) {
            selectedSound.mute();
            selectedSound.detach();
        }
        Main.getVehicle().setEngineSound(selectedSound);
        selectedSound = null;

        super.onDisable();
    }

    /**
     * Callback invoked whenever this menu becomes both attached and enabled.
     */
    @Override
    protected void onEnable() {
        super.onEnable();

        Sound engineSound = Main.getVehicle().getEngineSound();
        if (engineSound == null) {
            selectedSound = null;
        } else {
            try {
                selectedSound = engineSound.getClass().newInstance();
            } catch (IllegalAccessException | InstantiationException exception) {
                throw new RuntimeException(exception);
            }
            AssetManager assetManager = Main.getApplication().getAssetManager();
            selectedSound.load(assetManager);
            configureSelectedSound();
        }
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);

        if (selectedSound != null) {
            configureSelectedSound();
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Configure the selected Sound and attach it to the scene graph.
     */
    private void configureSelectedSound() {
        float pitch = 60f;
        float volume = AudioHud.effectiveVolume();
        selectedSound.setPitchAndVolume(pitch, volume);

        Node rootNode = Main.getApplication().getRootNode();
        selectedSound.attachTo(rootNode);
    }

    /**
     * Alter which Sound is selected.
     *
     * @param newSound the desired Sound (not loaded, alias created) or null for
     * silence
     */
    private void setSound(Sound newSound) {
        if (selectedSound != null) {
            selectedSound.mute();
            selectedSound.detach();
        }

        selectedSound = newSound;
        if (newSound != null) {
            AssetManager assetManager = Main.getApplication().getAssetManager();
            newSound.load(assetManager);
            configureSelectedSound();
        }
    }
}
