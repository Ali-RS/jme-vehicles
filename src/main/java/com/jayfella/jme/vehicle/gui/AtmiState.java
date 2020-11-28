package com.jayfella.jme.vehicle.gui;

import com.atr.jme.font.TrueTypeMesh;
import com.atr.jme.font.asset.TrueTypeKeyMesh;
import com.atr.jme.font.shape.TrueTypeNode;
import com.atr.jme.font.util.Style;
import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.mesh.RectangleOutlineMesh;
import jme3utilities.mesh.RoundedRectangle;

/**
 * AppState to manage an automatic-transmision mode indicator. This AppState
 * should be instantiated once and then enabled/disabled as needed.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class AtmiState extends BaseAppState {
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
            = Logger.getLogger(AtmiState.class.getName());
    // *************************************************************************
    // fields

    private Car car;
    /**
     * vertical spacing between indicator positions (in pixels)
     */
    private float spacingY;
    /**
     * dimensions of the GUI viewport (in pixels)
     */
    private float viewPortHeight, viewPortWidth;

    private Geometry lightGeometry;
    /**
     * pre-loaded materials
     */
    private Material backgroundMaterial, lightMaterial;
    /**
     * Node that represents the indicator
     */
    private Node node;

    private TrueTypeMesh droidFont;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled indicator.
     */
    AtmiState() {
        super("Automatic-Transmission Mode Indicator");
        setEnabled(false);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Associate a Car with this HUD prior to enabling it.
     *
     * @param car the Car to use (or null for none)
     */
    void setCar(Car car) {
        this.car = car;
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
     * Callback invoked during initialization once this AppState is attached but
     * before onEnable() is called.
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        Camera camera = app.getCamera();
        viewPortHeight = camera.getHeight();
        viewPortWidth = camera.getWidth();
        /*
         * pre-load the Droid font
         */
        AssetManager manager = app.getAssetManager();
        AssetKey<TrueTypeMesh> assetKey = new TrueTypeKeyMesh(
                "Interface/Fonts/DroidSerifBold-aMPE.ttf", Style.Plain, 18);
        droidFont = manager.loadAsset(assetKey);
        /*
         * pre-load unshaded materials for the mode indicator
         */
        backgroundMaterial
                = MyAsset.createUnshadedMaterial(manager, ColorRGBA.Black);
        lightMaterial
                = MyAsset.createUnshadedMaterial(manager, ColorRGBA.Green);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        hideAtmi();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        String[] atModes = car.listAtModes();
        showAtmi(atModes);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        /*
         * Indicate the current mode of the automatic transmission.
         */
        String mode;
        if (car.getGearBox().isReversing()) {
            mode = "R";
        } else {
            mode = "D";
        }
        String[] atModes = car.listAtModes();
        setMode(atModes, mode);
    }
    // *************************************************************************
    // private methods

    /**
     * Attach the specified Spatial to the GUI node.
     *
     * @param spatial (not null, alias created)
     */
    private void attachToGui(Spatial spatial) {
        SimpleApplication simpleApp = Main.getApplication();
        Node guiNode = simpleApp.getGuiNode();
        guiNode.attachChild(spatial);
    }

    /**
     * Hide the automatic-transmission mode indicator.
     */
    private void hideAtmi() {
        if (node != null) {
            node.removeFromParent();
            node = null;
        }
    }

    /**
     * Indicate the specified automatic-transmission mode.
     *
     * @param modes the array of modes, in top-to-bottom order
     * @param selectedMode which mode to indicate (not null)
     */
    private void setMode(String modes[], String selectedMode) {
        for (int i = 0; i < modes.length; ++i) {
            if (selectedMode.equals(modes[i])) {
                float y = (modes.length - i) * spacingY;
                lightGeometry.setLocalTranslation(0f, y, 0f);
                lightGeometry.setCullHint(Spatial.CullHint.Never);
                return;
            }
        }

        lightGeometry.setCullHint(Spatial.CullHint.Always);
    }

    /**
     * Display the automatic-transmission mode indicator.
     *
     * @param modes an array of modes, in top-to-bottom order
     */
    private void showAtmi(String[] modes) {
        hideAtmi();

        node = new Node("Automatic-Transmission Mode Indicator");
        attachToGui(node);
        float centerX = 0.625f * viewPortWidth;
        float bottomY = 0.15f * viewPortHeight;
        node.setLocalTranslation(centerX, bottomY, guiZ);

        spacingY = 0.032f * viewPortHeight;
        float yModeCenter = modes.length * spacingY;
        float maxWidth = 0f;
        int kerning = 0;
        ColorRGBA textColor = ColorRGBA.White.clone();
        /*
         * Attach a TrueTypeNode for each mode.
         */
        for (String mode : modes) {
            TrueTypeNode ttNode = droidFont.getText(mode, kerning, textColor);
            node.attachChild(ttNode);
            float width = ttNode.getWidth();
            float x = -width / 2f;
            float height = ttNode.getHeight();
            float y = yModeCenter + height / 2f;
            ttNode.setLocalTranslation(x, y, guiZ);

            if (width > maxWidth) {
                maxWidth = width;
            }
            yModeCenter -= spacingY;
        }
        /*
         * Attach a rounded-rectangle Geometry for the background.
         */
        float bgHeight = (modes.length + 1) * spacingY;
        float cornerRadius = 0.01f * viewPortWidth;
        float bgWidth = maxWidth + 2f * cornerRadius;
        Mesh bgMesh = new RoundedRectangle(-bgWidth / 2f, bgWidth / 2f, 0f,
                bgHeight, cornerRadius, 1f);
        Geometry bgGeometry = new Geometry("bg", bgMesh);
        node.attachChild(bgGeometry);
        bgGeometry.setLocalTranslation(0f, 0f, -0.1f);
        bgGeometry.setMaterial(backgroundMaterial);
        /*
         * Attach a rectangular outline Geometry for the indicator.
         */
        float indWidth = maxWidth + cornerRadius;
        float x1 = -indWidth / 2f;
        float x2 = indWidth / 2f;
        float y1 = -spacingY / 2f;
        float y2 = spacingY / 2f;
        Mesh indMesh = new RectangleOutlineMesh(x1, x2, y1, y2);
        lightGeometry = new Geometry("ind", indMesh);
        node.attachChild(lightGeometry);
        lightGeometry.setCullHint(Spatial.CullHint.Always);
        lightGeometry.setMaterial(lightMaterial);
    }
}
