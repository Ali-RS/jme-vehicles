package com.jayfella.jme.vehicle.gui;

import com.atr.jme.font.TrueTypeMesh;
import com.atr.jme.font.asset.TrueTypeKeyMesh;
import com.atr.jme.font.shape.TrueTypeNode;
import com.atr.jme.font.util.Style;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.input.DrivingInputState;
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
import com.simsilica.lemur.event.MouseEventControl;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.mesh.RoundedRectangle;

/**
 * AppState to display the name of the default camera. This AppState should be
 * instantiated once and then enabled/disabled as needed.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class CameraNameState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(CameraNameState.class.getName());
    // *************************************************************************
    // fields

    /**
     * pre-loaded Material for the background
     */
    private Material bgMaterial;
    /**
     * relevant spatials, to be attached to the GUI node
     */
    final private Node node = new Node("camera name");
    /**
     * text currently displayed, or null if the Node isn't populated
     */
    private String displayedText = null;
    /**
     * pre-loaded font for the text
     */
    private TrueTypeMesh droidFont;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled AppState.
     */
    CameraNameState() {
        super("Camera Name");
        setEnabled(false);
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
        /*
         * position the Node in the viewport
         */
        Camera camera = app.getCamera();
        float viewPortHeight = camera.getHeight();
        float viewPortWidth = camera.getWidth();
        float x = 0.62f * viewPortWidth;
        float y = 0.95f * viewPortHeight;
        node.setLocalTranslation(x, y, 0f);
        /*
         * pre-load the Droid font
         */
        AssetManager assetManager = app.getAssetManager();
        AssetKey<TrueTypeMesh> assetKey = new TrueTypeKeyMesh(
                "Interface/Fonts/DroidSerifBold-aMPE.ttf", Style.Plain, 18);
        droidFont = assetManager.loadAsset(assetKey);
        /*
         * pre-load the background Material
         */
        ColorRGBA bgColor = new ColorRGBA(0.1f, 0.2f, 0.5f, 1f);
        bgMaterial = MyAsset.createUnshadedMaterial(assetManager, bgColor);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        /*
         * Detach the Node from the GUI.
         */
        node.removeFromParent();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        updateNode();
        /*
         * Attach the Node to the GUI.
         */
        SimpleApplication simpleApp = (SimpleApplication) getApplication();
        Node guiNode = simpleApp.getGuiNode();
        guiNode.attachChild(node);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        updateNode();
    }
    // *************************************************************************
    // private methods

    /**
     * Repopulate the Node from scratch.
     */
    private void repopulateNode(String text) {
        node.detachAllChildren();
        displayedText = null;
        if (text == null) {
            return;
        }
        /*
         * Attach a TrueTypeNode for the text.
         */
        int kerning = 0;
        ColorRGBA textColor = ColorRGBA.White.clone();
        TrueTypeNode ttNode = droidFont.getText(text, kerning, textColor);
        node.attachChild(ttNode);
        float textWidth = ttNode.getWidth();
        float x = -textWidth / 2;
        float textHeight = ttNode.getHeight();
        float y = textHeight / 2;
        ttNode.setLocalTranslation(x, y, 1f);
        /*
         * Attach a rounded-rectangle Geometry for the background.
         */
        float cornerRadius = 0.25f * textHeight;
        float bgWidth = textWidth + 2 * cornerRadius;
        float bgHeight = textHeight + 2 * cornerRadius;
        float x1 = -bgWidth / 2;
        float x2 = bgWidth / 2;
        float y1 = -bgHeight / 2;
        float y2 = bgHeight / 2;
        float zNormal = +1f;
        Mesh bgMesh
                = new RoundedRectangle(x1, x2, y1, y2, cornerRadius, zNormal);
        Geometry background = new Geometry("camera name background", bgMesh);
        node.attachChild(background);
        background.setMaterial(bgMaterial);
        /*
         * Add an Expander to advance to the next camera mode.
         */
        Expander listener = new Expander(node) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    DrivingInputState inputState
                            = Main.findAppState(DrivingInputState.class);
                    inputState.nextCameraMode();
                }
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        node.addControl(control);

        displayedText = text;
    }

    /**
     * Compare the default camera name to the displayed text and repopulate the
     * Node if they differ.
     */
    private void updateNode() {
        Camera defaultCamera = getApplication().getCamera();
        String name = defaultCamera.getName();
        if (displayedText == null || !displayedText.equals(name)) {
            repopulateNode(name);
        }
    }
}
