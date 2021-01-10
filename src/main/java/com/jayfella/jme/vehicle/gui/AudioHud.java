package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Main;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.simsilica.lemur.event.MouseEventControl;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.mesh.DiscMesh;

/**
 * Heads-up display (HUD) for controlling audio. This AppState should be
 * instantiated once and then enabled/disabled as needed. It directly manages
 * the mute button.
 */
public class AudioHud extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * Z-coordinate for most GUI geometries
     */
    final private static float guiZ = 1f;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(AudioHud.class.getName());
    // *************************************************************************
    // fields

    /**
     * true&arr;audio is globally muted, false&rarr;audio enabled
     */
    private static boolean isGloballyMuted = false;
    /**
     * height of the GUI viewport (in pixels)
     */
    private float viewPortHeight;
    /**
     * width of the GUI viewport (in pixels)
     */
    private float viewPortWidth;
    /**
     * visualize the mute button
     */
    private Geometry muteButton;
    /**
     * indicate audio is muted
     */
    private Material muteMaterial;
    /**
     * indicate audio is sounding
     */
    private Material soundMaterial;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled HUD.
     */
    public AudioHud() {
        super("Audio HUD");
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Toggle the audio between enabled and muted.
     */
    public void toggleAudioMuted() {
        isGloballyMuted = !isGloballyMuted;
        showMuteButton();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Test whether sound is globally muted.
     *
     * @return true&rarr;muted, false&rarr;enabled
     */
    public static boolean isMuted() {
        return isGloballyMuted;
    }

    /**
     * Toggle the sound between muted and enabled.
     */
    public static void toggleMuted() {
        isGloballyMuted = !isGloballyMuted;
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void cleanup(Application app) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        Camera camera = app.getCamera();
        viewPortHeight = camera.getHeight();
        viewPortWidth = camera.getWidth();
        /*
         * pre-load unshaded materials for buttons
         */
        AssetManager manager = app.getAssetManager();
        Texture texture = manager.loadTexture("Textures/mute.png");
        muteMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/sound.png");
        soundMaterial = MyAsset.createUnshadedMaterial(manager, texture);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        hideMuteButton();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        showMuteButton();
    }
    // *************************************************************************
    // private methods

    /**
     * Attach the specified Spatial to the GUI node.
     *
     * @param spatial (not null, alias created)
     */
    private void attachToGui(Spatial spatial) {
        Node guiNode = Main.getApplication().getGuiNode();
        guiNode.attachChild(spatial);
    }

    /**
     * Hide the mute button.
     */
    private void hideMuteButton() {
        if (muteButton != null) {
            muteButton.removeFromParent();
            muteButton = null;
        }
    }

    /**
     * Display the mute/sound button.
     */
    private void showMuteButton() {
        hideMuteButton();

        float radius = 0.025f * viewPortHeight;
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        muteButton = new Geometry("mute button", mesh);
        attachToGui(muteButton);

        if (isGloballyMuted) {
            muteButton.setMaterial(muteMaterial);
        } else {
            muteButton.setMaterial(soundMaterial);
        }
        /*
         * Position the button in the viewport.
         */
        float x = 0.76f * viewPortWidth;
        float y = 0.95f * viewPortHeight;
        muteButton.setLocalTranslation(x, y, guiZ);
        /*
         * Add an Expander to toggle the sound on/muted.
         */
        Expander listener = new Expander(muteButton) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    toggleAudioMuted();
                }
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        muteButton.addControl(control);
    }
}
