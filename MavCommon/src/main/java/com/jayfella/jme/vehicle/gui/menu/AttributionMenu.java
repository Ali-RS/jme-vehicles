package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.gui.CompassState;
import com.jayfella.jme.vehicle.gui.lemur.AudioHud;
import com.jayfella.jme.vehicle.gui.lemur.CameraNameState;
import com.jayfella.jme.vehicle.gui.lemur.PhysicsHud;
import com.jayfella.jme.vehicle.lemurdemo.Main;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to display attributions.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class AttributionMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(AttributionMenu.class.getName());
    /**
     * payload message, to be displayed
     */
    final private static String attributionMessage
            = "This work is based on \"Opel GT Retopo\"\n"
            + "(https://sketchfab.com/3d-models/opel-gt-retopo-badcab3c8a3d42359c8416db8a7427fe)\n"
            + "by Thomas Glenn Thorne licensed under CC-BY-NC-SA.\n\n"
            + "This work is based on \"Ford Ranger\"\n"
            + "(https://sketchfab.com/3d-models/ford-ranger-dade78dc96e34f1a8cbcf14dd47d84de)\n"
            + "by mauro.zampaoli (https://sketchfab.com/mauro.zampaoli)\n"
            + "licensed under CC-BY-4.0 (http://creativecommons.org/licenses/by/4.0/).\n\n"
            + "This work is based on \"Nissan GT-R\"\n"
            + "(https://sketchfab.com/3d-models/nissan-gt-r-5f5781614c6f4ff4b7cb1d3cff9d931c)\n"
            + "by iSteven licensed under CC-BY-NC-SA.\n\n"
            + "This work is based on \"HCR2 Buggy\"\n"
            + "(https://sketchfab.com/3d-models/hcr2-buggy-a65fe5c27464448cbce7fe61c49159ef)\n"
            + "by oakar258 licensed under CC-BY-4.0 (http://creativecommons.org/licenses/by/4.0/).\n\n"
            + "This work is based on \"HCR2 Rotator\"\n"
            + "(https://sketchfab.com/3d-models/hcr2-rotator-f03e95525b4c48cfb659064a76d8cd53)\n"
            + "by oakar258 (https://sketchfab.com/oakar258)\n"
            + "licensed under CC-BY-4.0 (http://creativecommons.org/licenses/by/4.0/).\n\n"
            + "This work is based on \"Modern Hatchback - Low Poly model\"\n"
            + "(https://sketchfab.com/3d-models/modern-hatchback-low-poly-model-055ff8a21b8d4d279debca089e2fafcd)\n"
            + "by Daniel Zhabotinsky (https://sketchfab.com/DanielZhabotinsky)\n"
            + "licensed under CC-BY-4.0 (http://creativecommons.org/licenses/by/4.0/).";
    // *************************************************************************
    // fields

    /**
     * height of the GUI viewport (in pixels)
     */
    private float viewPortHeight;
    /**
     * width of the GUI viewport (in pixels)
     */
    private float viewPortWidth;
    /*
     * visual presentation
     */
    final private Node node = new Node("Attribution Node");
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected List<Button> createItems() {
        List<Button> result = new ArrayList<>(1);

        Button button = new Button("<< Back");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new MainMenu())
        ));
        result.add(button);

        return result;
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        super.initialize(application);

        Camera camera = application.getCamera();
        viewPortHeight = camera.getHeight();
        viewPortWidth = camera.getWidth();
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        node.removeFromParent();

        getState(AudioHud.class).setEnabled(true);
        getState(CameraNameState.class).setEnabled(true);
        getState(CompassState.class).setEnabled(true);
        getState(PhysicsHud.class).setEnabled(true);

        super.onDisable();
    }

    /**
     * Callback invoked whenever this menu becomes both attached and enabled.
     */
    @Override
    protected void onEnable() {
        super.onEnable();

        getState(AudioHud.class).setEnabled(false);
        getState(CameraNameState.class).setEnabled(false);
        getState(CompassState.class).setEnabled(false);
        getState(PhysicsHud.class).setEnabled(false);

        node.detachAllChildren();
        setupShutter();
        setupText();
        Main.getApplication().getGuiNode().attachChild(node);
    }
    // *************************************************************************
    // private methods

    /**
     * Create and attach a Quad to hide what's happening in the main scene.
     *
     * @return a new instance
     */
    private void setupShutter() {
        AssetManager assetManager = Main.getApplication().getAssetManager();
        Material material = new Material(assetManager, Materials.UNSHADED);
        material.setColor("Color", new ColorRGBA(0f, 0f, 0f, 1f));

        Mesh mesh = new Quad(viewPortWidth, viewPortHeight);
        Geometry shutter = new Geometry("shutter", mesh);
        node.attachChild(shutter);

        shutter.move(0f, 0f, -1f);
        shutter.setMaterial(material);
    }

    /**
     * Create and attach a TrueTypeNode containing the attribution message.
     */
    private void setupText() {
        Main application = Main.getApplication();
        AssetManager assetManager = application.getAssetManager();
        BitmapFont bigFont
                = assetManager.loadFont("/Interface/Fonts/Default.fnt");

        BitmapText bitmapText = new BitmapText(bigFont);
        bitmapText.setText(attributionMessage);
        node.attachChild(bitmapText);

        float scaleFactor = 1.31f;
        bitmapText.setLocalScale(scaleFactor);

        float textWidth = scaleFactor * bitmapText.getLineWidth();
        float x = (viewPortWidth - textWidth) / 2;
        float textHeight = scaleFactor * bitmapText.getHeight();
        float y = (viewPortHeight + textHeight) / 2;
        bitmapText.setLocalTranslation(x, y, 0f);
    }
}
